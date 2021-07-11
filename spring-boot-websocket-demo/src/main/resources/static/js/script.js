'use strict'

let stompClient

const connect = () => {
    // stompClient = Stomp.client('ws://127.0.0.1:9000/socket');

    const socket = new SockJS('/socket')
    stompClient = Stomp.over(socket)
    stompClient.connect({}, onConnected, onError)
}

const onConnected = () => {
    stompClient.subscribe('/topic/quotation', onMessageReceived)
    stompClient.subscribe('/topic/chat', onMessageReceived)
    stompClient.subscribe('/user/topic/login', onMessageReceived)
    stompClient.subscribe('/topic/broadcast', onMessageReceived)
}

const onError = (error) => {
    console.error('connection failure: ' + error)
}

const onMessageReceived = (payload) => {
    console.log(payload)
}

const jsonSubmit = () => {
    let payload = JSON.stringify({ username: 'testuser', content: 'Hello world!', type: 'CHAT', time: '2021-07-11T16:20:20' })
    let header = {}
    stompClient.send('/app/chat/send', header, payload)
}

const textSubmit = () => {
    let payload = 'username'
    let header = {}
    stompClient.send('/app/login', header, payload)
}

const topicSubmit = () => {
    let payload = 'broadcast'
    let header = {}
    stompClient.send('/topic/broadcast', header, payload)
}

document.getElementById('connect').addEventListener('click', connect)
document.getElementById('json-submit').addEventListener('click', jsonSubmit)
document.getElementById('text-submit').addEventListener('click', textSubmit)
document.getElementById('topic-submit').addEventListener('click', topicSubmit)