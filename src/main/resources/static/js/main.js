'use strict';

var usernamePage = document.querySelector('#username-page');
var chatPage = document.querySelector('#chat-page');
var userPage = document.querySelector('#user-page');
var usernameForm = document.querySelector('#usernameForm');
var messageForm = document.querySelector('#messageForm');
var messageInput = document.querySelector('#message');
var friendArea = document.querySelector('#friendArea');
var messageArea = document.getElementById('messageArea');
var friendPage = document.getElementById('friend-page');
var connectingElement = document.querySelector('.connecting');
var signInPage = document.querySelector('#sign-in-page');
var count = 0;
var stompClient = null;
var username = null;
var password = null;
var email = null;
var onlineUsers = null;
var recipient = null;

var colors = [
    '#2196F3', '#32c787', '#00BCD4', '#ff5652',
    '#ffc107', '#ff85af', '#FF9800', '#39bbb0'
];


friendArea.addEventListener('click', e => {
  const li = e.target.closest('li.friend-message');
  if (!li) return;
  while (messageArea.firstChild) {
    messageArea.removeChild(messageArea.firstChild);
  }
  const user = li.querySelector('span')?.textContent.trim();
  console.log('Delegated click on:', user);
  li.classList.toggle('selected');
  recipient = user
  sendOldMessages();

});





function connect(event) {
    event.preventDefault(); // Prevent form submission

    username = document.querySelector('#name').value.trim();
    password = document.querySelector('#password').value.trim();
    email = document.querySelector('#email').value.trim();
//    recipient = document.querySelector('#recipient').value.trim();
    if (username && password && email) {
//        fetch('loginPage/api/exists/' + recipient)
//            .then(res => res.json())
//            .then(exists => {
//                if (exists) {
                    usernamePage.classList.add('hidden');
                    chatPage.classList.remove('hidden');
                    friendPage.classList.remove('hidden');
                    var socket = new SockJS('/ws');
                    stompClient = Stomp.over(socket);
                    stompClient.connect({}, onConnected, onError);
//                } else {
//                    console.log("User does not exist");
//                }
////            })
//            .catch(error => {
//                console.error('Error checking user existence:', error);
//            });
    } else {
        console.log("Please fill in all fields.");
    }
}

function onConnected() {
    stompClient.subscribe('/user/' + username + '/queue/receive', onMessageReceived);
    sendOldMessages();
    while (friendArea.firstChild) {
        friendArea.removeChild(friendArea.firstChild);
      }

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
//async function fetchOnlineUsers() {
//  try {
//    const response = await fetch('/loginPage/api/onlineUsers', {
//      method: 'GET',
//      headers: {
//        'Accept': 'application/json'
//      }
//    });
//
//    if (!response.ok) {
//      // Handle HTTP errors
//      throw new Error(`Network response was not ok (${response.status} ${response.statusText})`);
//    }
//
//    // Parse and return JSON array of usernames
//    const onlineUsers = await response.json();
//
//    if (!Array.isArray(onlineUsers)) {
//      throw new Error('Invalid data format: expected an array of strings');
//    }
//
//    return onlineUsers;
//  } catch (error) {
//    console.error('Failed to fetch online users:', error);
//    // Depending on your app, you might rethrow, return a default value, or update the UI
//    throw error;
//  }
//}


function createUser(message) {
  console.log('👉 createUser()', message);
  try {
     if(message.sender.username == username){
     return;
    }
//    friendPage.classList.remove('hidden');
    const li = document.createElement('li');
    li.classList.add('friend-message');

    const av = document.createElement('i');
//    av.textContent = message.sender.username;
    av.style.backgroundColor = getAvatarColor(message.sender.username);
    li.appendChild(av);

    const name = document.createElement('span');
    name.textContent = ' ' + message.sender.username;
    li.appendChild(name);

    friendArea.appendChild(li);
    friendArea.scrollTop = friendArea.scrollHeight;
    console.log('✅ appended li', li);
  } catch(err) {
    console.error('❌ createUser() error:', err);
  }
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
        stompClient.send("/app/chat.sendPrivateMessage/" + username, {}, JSON.stringify(chatMessage));
        messageInput.value = ''; // Clear input after sending
    }
}


async function updateFriendArea()
{
  while (friendArea.firstChild) {
    friendArea.removeChild(friendArea.firstChild);
  }

  const response = await fetch('/loginPage/api/onlineUsers', {
          method: 'GET',
          headers: { 'Accept': 'application/json' }
        });

        if (!response.ok) {
          throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }

        // Parse the JSON array into a JS Array<string>
       const onlineUsers = await response.json();
       console.log('Online users:', onlineUsers);
      // 1) Get current users
  //   const usersRes = await fetch('loginPage/api/users');
  //    if (!usersRes.ok) throw new Error('Failed to fetch users');
  //    const users = await usersRes.json();
       onlineUsers.forEach(u => createUser({ sender: u }));
}

updateFriendArea();

const timer = setInterval(updateFriendArea, 10000);

async function sendOldMessages() {
  try {

  const response = await fetch('/loginPage/api/onlineUsers', {
        method: 'GET',
        headers: { 'Accept': 'application/json' }
      });

      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }

      // Parse the JSON array into a JS Array<string>
     const onlineUsers = await response.json();
     console.log('Online users:', onlineUsers);
    // 1) Get current users
//   const usersRes = await fetch('loginPage/api/users');
//    if (!usersRes.ok) throw new Error('Failed to fetch users');
//    const users = await usersRes.json();
friendPage.classList.remove('hidden');

    if (count ==0){
      count = count +1;
    // Populate friends list
      onlineUsers.forEach(u => createUser({ sender: u }));
    }
    var sender_recipient = username+recipient;
    console.log('USERRRRRRRRRRRRR', sender_recipient)
    // 2) Get historical chat messages
    const msgsRes = await fetch("/api/messages/" + sender_recipient);
    if (!msgsRes.ok) throw new Error('Failed to fetch messages');
    const messages = await msgsRes.json();   // e.g. [{sender,recipient,content,timestamp},…]
    console.log('Historical messages:', messages);
//    const count = 0;
    for (const m of messages) {
//    count = count +1;
//    console.log('COUNT', count);
    const chatMessage = {
      sender:    m.sender,
      recipient: m.recipient,
      content:   m.content,
      type:      'CHAT'
    };
    stompClient.send(
    `/app/chat.sendOldMessage/${username}`,
    {},
    JSON.stringify(chatMessage)
    );
    await new Promise(r => setTimeout(r, 20));
    }
  }
  catch (err) {
    console.error('sendOldMessages error:', err);
  }
}

function onMessageReceived(payload) {
    var message = JSON.parse(payload.body);
    var messageElement = document.createElement('li');

     if (message.content === 'Leave') {
        messageElement.classList.add('event-message');
        messageElement.textContent = "${message.sender} has left!";
        console.log('Online users: ',onlineUsers);
      }
     else {
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


//function newUserOnline(message)
//{
//   try {
//     if(message.sender.username == username){
//     return;
//    }
//    friendPage.classList.remove('hidden');
//    const li = document.createElement('li');
//    li.classList.add('friend-message');
//
//    const av = document.createElement('i');
////    av.textContent = message.sender.username;
//    av.style.backgroundColor = getAvatarColor(message.sender.username);
//    li.appendChild(av);
//
//    const name = document.createElement('span');
//    name.textContent = ' ' + message.sender.username;
//    li.appendChild(name);
//
//    friendArea.appendChild(li);
//    friendArea.scrollTop = friendArea.scrollHeight;
//    console.log('✅ appended li', li);
//    } catch(err) {
//      console.error('❌ createUser() error:', err);
//    }
//}



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
