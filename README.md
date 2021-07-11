# Demo project - Socket with Spring Boot

- Basic knowledge of socket
    - [Documents](https://github.com/jake-tw/documents/blob/master/network/internet-protocol.md)

- Java Plain Socket

    - Server

    - Client

- Spring Boot WebSocket

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