## Demo project - Socket with Spring Boot

#### Basic knowledge of Socket

- [Documents](https://github.com/jake-tw/documents/blob/master/network/internet-protocol.md)

#### Java Plain Socket

- Server

    - TCP: ServerSocket

    - UDP: DatagramSocket

- Client

    - TCP: Socket

    - UDP: DatagramPacket

- NIO

    - TCP: ServerSocketChannel

    - UDP: DatagramChannel

<br>

#### Spring Boot WebSocket

- Config

    ```java
    @Configuration
    @EnableWebSocketMessageBroker
    public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
        ...
    }
    ```

    - configureMessageConverters: 設定接收傳送資料的 Converter，依照 content-type 決定使用哪個 Converter，回傳 false 取消預設 Converter

        ```java
        @Override
        public boolean configureMessageConverters(List<MessageConverter> messageConverters) {
            messageConverters.add(new MyConverter());
            return false;
        }
        ```

        - 預設 Converter 支援的 MINE Type

            - text/plain

            - application/json
                
                - 優先順序 jackson2 > gson > jsonb > kotlinSerializationJson

            - application/octet-stream

    - registerStompEndpoints: 註冊連線位置

        ```java
        @Override
        public void registerStompEndpoints(StompEndpointRegistry registry) {
            registry.addEndpoint("/socket"); // receive ws://
            registry.addEndpoint("/socket").withSockJS(); // receive htt://
        }
        ```

        - addEndpoint 註冊 WebSocket 連線位置，加上 withSockJS 則表示此位置支援 SockJS 連線，若是要保留 WebSocket 連線可多加一行不含 withSockJS 的 Endpoint

    - configureMessageBroker

        ```java
        @Override
        public void configureMessageBroker(MessageBrokerRegistry registry) {
            registry.setApplicationDestinationPrefixes("/app");
            registry.enableSimpleBroker("/topic");
            registry.setUserDestinationPrefix("/user");
        }
        ```

        - setApplicationDestinationPrefixes: 所有 Server 接收 Message 的 destinations 都要包含 Prefixes

        - enableSimpleBroker: 允許那些 Channel 向使用者發送 Message

        - setUserDestinationPrefix: 預設 /user，轉換發送與接收的 Channel，讓 Channel 可以對應單一使用者，注意此處的 username 指的是 java.security.Principal 的 username

            - Server side: 
            
                - @SendToUser: 向 Client 時推送 Message 會自動在 destination 前面加上 /{prefix}/{username}，可指定是否用 broadcast 的形式推送

                - 推送使用如 /{prefix}/{username}/topic/transfer 含有 prefix 的 destination 會自動轉換為 /topic/transfer{uniqueId}

                - 主動向特定 Client 推送訊息需加上 

            - Client side: 訂閱時加上 prefix 如 /{prefix}/topic/transfer，自動轉換為訂閱 /topic/transfer{uniqueId}

- SimpMessageSendingOperations: 可 Autowired 此 Interface 主動向使用者推送 Message

    ```java
    simpMessageSendingOperations.convertAndSend(destination, payload);
    simpMessageSendingOperations.convertAndSendToUser(user, destination, payload);
    ```

- EventListener: 標註 @EventListener 的方法可用來處理特定 Event

    ```java
    @EventListener
    public void handleConnectEventListener(SessionConnectEvent event) {
        ...
    }

    @EventListener
    public void handleDisconnectEventListener(SessionDisconnectEvent event) {
        ...
    }
    ```

- Principal

    - HandshakeInterceptor: 可以在 beforeHandshake 進行身分認證，回傳 boolean 同意或拒絕握手

    - DefaultHandshakeHandler: Override determineUser 回傳定義好的 Principal，此 Principal 可以在 Controller 的方法中做為參數自動傳遞

- Annotation

    - @Controller: 表示處理 Message 的 Class

    - @MessageMapping: 接收 Message 的 Destination

    - @SendTo: 推送 Message 到指定 Topic

    - @SendToUser: 推送 Message 到指定 Topic，可選擇是否只推送給特定 User

    - @Payload: 表示該參數為 Body 的資料

<br>

#### Spring Boot RSocket

- TCP Connection

    - Properties: 此處的 port 是指 RSocket Server 的 port，若是同時使用 Spring WebFlux 則會有兩個 port，一個 Web Server，一個 RSocket Server

        ```txt
        server.port=9000
        spring.rsocket.server.port=7000
        ```

    - Requester Example

        ```java
        RSocketStrategies strategies = RSocketStrategies.builder()
            .encoders(encoders -> encoders.add(new Jackson2CborEncoder()))
            .decoders(decoders -> decoders.add(new Jackson2CborDecoder()))
            .build();

        RSocketRequester requester = RSocketRequester.builder().rsocketStrategies(strategies).tcp("127.0.0.1", 7000);
        ```

- WebSocket Connection

    - Properties: 若透過 Web Server 握手則需要指定 path，如 http://127.0.0.1:{server.port}/rsocket 或 ws://127.0.0.1:{server.port}/rsocket，也可以直接透過 RSocket Server 的 port 進行連線

        ```txt
        spring.rsocket.server.mapping-path=/rsocket
        spring.rsocket.server.transport=websocket
        ```

    - Requester Example

        ```java
        RSocketStrategies strategies = RSocketStrategies.builder()
            .encoders(encoders -> encoders.add(new Jackson2CborEncoder()))
            .decoders(decoders -> decoders.add(new Jackson2CborDecoder()))
            .build();

        URI url = URI.create("http://127.0.0.1:" + {server.port} + "/rsocket");
        RSocketRequester requester = RSocketRequester.builder().rsocketStrategies(strategies).websocket(url);
        ```

- 透過 Responder 回應 Server 的 Request

    1. 在 Client 新增處理 Server 請求方法的 Handler

        ```java
        class ClientHandler {
            @MessageMapping("client-status")
            public Flux<String> statusUpdate(String status) {
                log.info("Connection {}", status);
                return Flux.interval(Duration.ofSeconds(5)).map(index -> String.valueOf(Runtime.getRuntime().freeMemory()));
            }
        }
        ```

    2. 使用 Handler 建立 Responder，並加入 RsocketConnector

        ```java
        SocketAcceptor responder = RSocketMessageHandler.responder(strategies, new ClientHandler());

        requester = RSocketRequester.builder()
            .setupRoute("shell-client")
            .setupData(client)
            .rsocketStrategies(strategies)
            .rsocketConnector(connector -> connector.acceptor(responder))
            .tcp("127.0.0.1", port);
        ```

    3. Server 發送請求

        ```java
        requester.route("client-status")
                    .data("OPEN")
                    .retrieveFlux(String.class)
                    .doOnNext(s -> log.info("Client: {} Free Memory: {}.", client, s))
                    .subscribe();
        ```

- Annotation:

    - @Controller: 表示為處理 Message 的 Class

    - @ConnectMapping: 接收 SETUP 類型的請求，必須回傳 Void，可以與 @MessageMapping 組合使用

    - @MessageMapping: 接收請求，依照傳入與回傳的參數區分 4 種 RSocket 的操作類型