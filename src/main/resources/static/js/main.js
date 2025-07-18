'use strict';

var usernamePage = document.querySelector('#username-page');
var chatPage = document.querySelector('#chat-page');
var userPage = document.querySelector('#user-page');
var usernameForm = document.querySelector('#usernameForm');
var messageForm = document.querySelector('#messageForm');
var messageInput = document.querySelector('#message');
var friendPage = document.getElementById('friend-page');
var messageArea = document.querySelector('#messageArea');
var friendArea = document.querySelector('#friendArea');
var connectingElement = document.querySelector('.connecting');
var signInPage = document.querySelector('#sign-in-page');

var stompClient = null;
var username = null;
var password = null;
var email = null;
var recipient = null;

var colors = [
    '#2196F3', '#32c787', '#00BCD4', '#ff5652',
    '#ffc107', '#ff85af', '#FF9800', '#39bbb0'
];

function connect(event) {
    event.preventDefault(); // Prevent form submission

    username = document.querySelector('#name').value.trim();
    password = document.querySelector('#password').value.trim();
    email = document.querySelector('#email').value.trim();
    recipient = document.querySelector('#recipient').value.trim();

    if (username && password && email && recipient) {
        fetch('loginPage/api/exists/' + recipient)
            .then(res => res.json())
            .then(exists => {
                if (exists) {
                    usernamePage.classList.add('hidden');
                    chatPage.classList.remove('hidden');
                    friendPage.classList.remove('hidden');
                    var socket = new SockJS('/ws');
                    stompClient = Stomp.over(socket);
                    stompClient.connect({}, onConnected, onError);
                } else {
                    console.log("User does not exist");
                }
            })
            .catch(error => {
                console.error('Error checking user existence:', error);
            });
    } else {
        console.log("Please fill in all fields.");
    }
}

function onConnected() {
    stompClient.subscribe('/user/' + username + '/queue/receive', onMessageReceived);

    sendOldMessages();
    stompClient.send("/app/chat.addUser", {}, JSON.stringify({
        sender: username,
        senderPassword: password,
        senderEmail: email,
        recipient: recipient,
        type: 'JOIN'
    }));
    connectingElement.classList.add('hidden');
}
//
function createUser(message) {
    var friendElement = document.createElement('li');
    friendElement.classList.add('friend-message');

    var avatarElement = document.createElement('i');
    var avatarText = document.createTextNode(message.sender[0].toUpperCase());
    avatarElement.appendChild(avatarText);
    avatarElement.style['background-color'] = getAvatarColor(message.sender);
    friendElement.appendChild(avatarElement);

    var usernameElement = document.createElement('span');
    var usernameText = document.createTextNode(' ' + message.sender);
    usernameElement.appendChild(usernameText);
    friendElement.appendChild(usernameElement);

    friendArea.appendChild(friendElement);
    friendArea.scrollTop = friendArea.scrollHeight;
}


function onError(error) {
    connectingElement.textContent = 'Could not connect to WebSocket server. Please refresh this page to try again!';
    connectingElement.style.color = 'red';
}

function sendMessage(event) {
    event.preventDefault(); // Prevent form submission

    var messageContent = messageInput.value.trim();
    if (messageContent && stompClient) {
        var chatMessage = {
            sender: username,
            content: messageContent,
            recipient: recipient,
            type: 'CHAT'
        };

        // Send the message to the recipient
        stompClient.send("/app/chat.sendPrivateMessage/" + recipient, {}, JSON.stringify(chatMessage));
        stompClient.send("/app/chat.sendPrivateMessage/" + username, {}, JSON.stringify(chatMessage))
        messageInput.value = ''; // Clear input after sending
    }
}

async function sendOldMessages() {
    try {
        const response = await fetch('/api/messages');
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        const messages = await response.json();
        console.log('Messages:', messages);

        for (const message of messages) {
            var chatMessage = {
                sender: message.sender,
                content: message.content,
                recipient: message.recipient,
                type: 'CHAT'
            };

            if (message.sender === username || message.recipient === username) {
                let messageSent = false;

                while (!messageSent) {
                    try {
                        stompClient.send("/app/chat.sendOldMessage/" + username, {}, JSON.stringify(chatMessage));
                        messageSent = true;
                    } catch (error) {
                        console.error('Error sending message:', error);
                        await new Promise(resolve => setTimeout(resolve, 100));
                    }
                }

                await new Promise(resolve => setTimeout(resolve, 10));
            }
        }
    } catch (error) {
        console.error('Error fetching messages:', error);
    }
}

function onMessageReceived(payload) {
    var message = JSON.parse(payload.body);
    var messageElement = document.createElement('li');

    if (message.type === 'JOIN') {
        messageElement.classList.add('event-message');
        message.content = message.sender + ' joined!';
        createUser(message); // <- Show in friend list
    } else if (message.type === 'LEAVE') {
        messageElement.classList.add('event-message');
        message.content = message.sender + ' left!';
    } else {
        messageElement.classList.add('chat-message');

        var avatarElement = document.createElement('i');
        var avatarText = document.createTextNode(message.sender[0]);
        avatarElement.appendChild(avatarText);
        avatarElement.style['background-color'] = getAvatarColor(message.sender);
        messageElement.appendChild(avatarElement);

        var usernameElement = document.createElement('span');
        var usernameText = document.createTextNode(message.sender);
        usernameElement.appendChild(usernameText);
        messageElement.appendChild(usernameElement);
    }

    var textElement = document.createElement('p');
    var messageText = document.createTextNode(message.content || "Message content is missing.");
    textElement.appendChild(messageText);
    messageElement.appendChild(textElement);

    messageArea.appendChild(messageElement);
    messageArea.scrollTop = messageArea.scrollHeight;
}



function getAvatarColor(messageSender) {
    var hash = 0;
    for (var i = 0; i < messageSender.length; i++) {
        hash = 31 * hash + messageSender.charCodeAt(i);
    }
    var index = Math.abs(hash % colors.length);
    return colors[index];
}

// Attach event listeners
usernameForm.addEventListener('submit', connect);
messageForm.addEventListener('submit', sendMessage);
