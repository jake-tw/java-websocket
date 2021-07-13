'use strict'

let stompClient

const connect = () => {
    // stompClient = Stomp.client('ws://127.0.0.1:9000/socket')

    const socket = new SockJS('/socket')
    stompClient = Stomp.over(socket)
    stompClient.connect({}, onConnected, onError)
}

const disconnect = () => {
    stompClient.disconnect()
}

const onConnected = () => {
    stompClient.subscribe('/topic/quotation', onQuotationReceived)
    stompClient.subscribe('/topic/chat', onMessageReceived)
    stompClient.subscribe('/user/topic/echo', onEchoReceived)
    stompClient.subscribe('/topic/broadcast', onBroadcastReceived)
}

const onError = (error) => {
    console.error('connection failure: ' + error)
}

const quotationContent = document.getElementById('quotation-content')
const onQuotationReceived = (payload) => {
	quotationContent.innerHTML += payload + '<br>'
}

const chatContent = document.getElementById('chat-content')
const onMessageReceived = (payload) => {
	chatContent.innerHTML = payload
}

const echoContent = document.getElementById('echo-content')
const onEchoReceived = (payload) => {
	echoContent.innerHTML = payload
}

const broadcastContent = document.getElementById('broadcast-content')
const onBroadcastReceived = (payload) => {
	broadcastContent.innerHTML = payload
}

const chatSubmit = () => {
    let payload = JSON.stringify({ username: 'testuser', content: 'Hello world!', type: 'CHAT', time: '2021-07-11T16:20:20' })
    let header = {}
    stompClient.send('/app/chat/send', header, payload)
}

const echoSubmit = () => {
    let payload = 'Greeting!'
    let header = {}
    stompClient.send('/app/echo', header, payload)
}

const topicBroadcast = () => {
    let payload = 'Hello everyone'
    let header = {}
    stompClient.send('/topic/broadcast', header, payload)
}

document.getElementById('connect').addEventListener('click', connect)
document.getElementById('disconnect').addEventListener('click', disconnect)
document.getElementById('chat-submit').addEventListener('click', chatSubmit)
document.getElementById('echo-submit').addEventListener('click', echoSubmit)
document.getElementById('topic-broadcast').addEventListener('click', topicBroadcast)