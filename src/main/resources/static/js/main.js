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
const chatHeader = document.querySelector('.chat-header');
var latitude1 = null;
var longitude1 = null;
var latitude2 = null;
var longitude2 = null;
var count = 0;
var distances = [];
var stompClient = null;
var username = null;
var file = null;
var password = null;
var email = null;
var recipient = null;

var colors = [
    '#2196F3', '#32c787', '#00BCD4', '#ff5652',
    '#ffc107', '#ff85af', '#FF9800', '#39bbb0'
];



function getCurrentLocation() {
  return new Promise((resolve, reject) => {
    if (!navigator.geolocation) {
      return reject(new Error('Geolocation not supported'));
    }
    navigator.geolocation.getCurrentPosition(
      pos => {
        const { latitude, longitude } = pos.coords;
        resolve({ lat: latitude, lon: longitude });
      },
      err => reject(err),
      { enableHighAccuracy: true, timeout: 10000 }
    );
  });
}

 async function getCountryFromCoords(lat, lon) {
      const userEmail = 'sancho.pancho1704@gmail.com';
      const url =
        `https://nominatim.openstreetmap.org/reverse?` +
        `format=jsonv2&lat=${lat}&lon=${lon}` +
        `&email=${encodeURIComponent(userEmail)}`;
      const res = await fetch(url);
      if (!res.ok) throw new Error(`Nominatim error ${res.status}`);
      const json = await res.json();
      return json.address?.country || null;
    }

async function getDrivingDistance(lat1, lon1, lat2, lon2) {
  const url = `https://router.project-osrm.org/route/v1/driving/`
      + `${lon1},${lat1};${lon2},${lat2}?overview=false`;

  const res = await fetch(url);
  const json = await res.json();
  if (json.code !== 'Ok') throw new Error(json.code);
  // metres → kilometres
  return json.routes[0].distance / 1000;
}
document.addEventListener('DOMContentLoaded', () => {
  const icon  = document.getElementById('profileIcon');
  const panel = document.getElementById('profilePanel');

  icon.addEventListener('click', e => {
    e.preventDefault();
    e.stopPropagation();
    panel.classList.toggle('open');
  });

  document.addEventListener('click', e => {
    if (!icon.contains(e.target) && !panel.contains(e.target)) {
      panel.classList.remove('open');
    }
  });
});
function initProfileModal() {
  const openBtn  = document.querySelector('#profilePanel .value:first-of-type');
  const modal    = document.getElementById('pf-modal');
  const content  = modal.querySelector('.pf-modal-content');
  const closeBtn = document.getElementById('pf-close');
  const updateBtn  = document.getElementById('pf-update-username');
  const changeBtn  = document.getElementById('pf-change-password');

  const userDisplay = document.getElementById('pf-username-display');
  const passDisplay = document.getElementById('pf-password-display');

  function populateProfileInfo() {
    userDisplay.textContent = username;
    passDisplay.textContent = '●'.repeat(password.length);
  }

  const fileInput = document.getElementById('file');
    if (!fileInput) {
      console.error('File input not found in DOM');
    } else {
      fileInput.addEventListener('change', e => {
        const file_f = e.target.files && e.target.files[0];
        if (file) {
          file = file_f;
          console.log('Selected file:', file);
        } else {
          console.log('No file selected');
        }
      });
    }

  openBtn.addEventListener('click', e => {
    e.preventDefault();
    e.stopPropagation();
    populateProfileInfo();
    modal.classList.remove('hidden');
  });
  updateBtn.addEventListener('click', () => {
    userDisplay.innerHTML = `
      <span style="margin-right:8px">Username:</span>
      <input type="text" class="pf-input-edit" value="${username}">
    `;
    const input = userDisplay.querySelector('input');
    input.focus();
  });

  changeBtn.addEventListener('click', () => {
    passDisplay.innerHTML = `
      <span style="margin-right:8px">Password:</span>
      <input type="password" class="pf-input-edit">
    `;
    const input = passDisplay.querySelector('input');
    input.focus();
  });
closeBtn.addEventListener('click', async () => {
  const ui = userDisplay.querySelector('input');
  const pi = passDisplay.querySelector('input');
  let exists;
  // Fetch existing users
  let users = [];
  try {
    users = await getJson("/loginPage/api/users");
  } catch (err) {
    console.error("Failed to load users:", err);
  }

  if (ui) {
    const newUsername = ui.value.trim();
    const { lat, lon } = await getCurrentLocation();
    exists = users.some(u => u.username === newUsername);
    if (!exists && newUsername) {
      if(pi.value)
      {
      const url = `/loginPage/api/updateUser/${newUsername}/${pi.value}/${username}`;
      const resp = await fetch(url);
      if (!resp.ok) throw new Error(`Error ${resp.status}`);
      updateOnlineUser(newUsername, pi.value, email);
      }
      else
      {
      const url = `/loginPage/api/updateUsername/${newUsername}/${username}`;
      const resp = await fetch(url);
      if (!resp.ok) throw new Error(`Error ${resp.status}`);
      updateOnlineUser(newUsername, password, email);
      }
      updateUserAndCoordinates(newUsername, lat, lon, username);
      username = newUsername;
    } else if (exists) {
      alert("Username already taken!");
    }
  }

  if (pi) {
    const newPass = pi.value;
    if (newPass) {
      password = newPass;
    }
  }
  populateProfileInfo();
  if(!exists){
  modal.classList.add('hidden');
  }



});


  modal.addEventListener('click', e => {
    if (!content.contains(e.target)) modal.classList.add('hidden');
  });
  content.addEventListener('click', e => e.stopPropagation());
}



let onlineUsers = [];

async function fetchOnlineUsers() {
  const res = await fetch('/loginPage/api/onlineUsers', {
    method: 'GET',
    headers: { 'Accept': 'application/json' }
  });

  if (!res.ok) {
    throw new Error(`Failed to fetch online users: ${res.status} ${res.statusText}`);
  }

  const data = await res.json();
  if (!Array.isArray(data)) {
    throw new Error(`Unexpected response shape: ${JSON.stringify(data)}`);
  }

  return data;
}
const inputs = document.querySelectorAll(".form__input");
function addfocus(){
    let parent = this.parentNode.parentNode
    parent.classList.add("focus")
}

function remfocus(){
    let parent = this.parentNode.parentNode
    if(this.value == ""){
        parent.classList.remove("focus")
    }
}

inputs.forEach(input=>{
    input.addEventListener("focus",addfocus)
    input.addEventListener("blur",remfocus)
})


// Ensure these globals exist somewhere in your module/script:
// let username = 'currentUser';
// let recipient = null;
// const friendArea = document.getElementById('friendArea');
// const messageArea = document.getElementById('messageArea');
// const chatHeader = document.getElementById('chatHeader');

friendArea.addEventListener('click', async (e) => {
  // 1. Find the clicked friend <li>
  const li = e.target.closest('li.friend-message');
  if (!li) return;

  // 2. Clear any previous selection and mark this li as selected
  friendArea
    .querySelectorAll('li.friend-message.selected')
    .forEach(el => el.classList.remove('selected'));
  li.classList.add('selected');

  // 3. Clear message area
  messageArea.replaceChildren();  // IE-safe alternative: while (messageArea.firstChild) { messageArea.removeChild(messageArea.firstChild); }

  // 4. Get the recipient username
  const userSpan = li.querySelector('span');
  const user = userSpan?.textContent.trim();
  if (!user) return;

  recipient = user;
chatHeader.innerHTML = `
  <div class="header-content">
    <div class="recipient-wrapper">
      <span class="recipient-name">${recipient}</span>
      <div class="loader-overlay">
        <div class="loader-container">
          <div class="loader"></div>
          <div class="loader-text">Loading…</div>
        </div>
      </div>
    </div>
  </div>

`;



  // 5. Load old messages (if asynchronous, await it)
  try {
    await sendOldMessages();
  } catch (err) {
    console.error('Failed to load old messages:', err);
    chatHeader.textContent = `${recipient} – messages unavailable`;
    return;
  }

  // 6. Fetch all user positions
  let userPositions;
  try {
    const resp = await fetch('/loginPage/api/userPositions');
    if (!resp.ok) {
      throw new Error(`Positions fetch failed (HTTP ${resp.status})`);
    }
    userPositions = await resp.json();
  } catch (err) {
    console.error('Error fetching positions:', err);
    chatHeader.textContent = `${recipient} – data unavailable`;
    return;
  }

  // 7. Find this user’s position
  const pos = userPositions.find(p => p.username === recipient);
  if (!pos || recipient === username) {
    chatHeader.textContent = `${recipient} – location unknown`;
    return;
  }

  // 8. Get the current user’s location
  let current;
  try {
    current = await getCurrentLocation(); // should return { lat, lon }
  } catch (err) {
    console.error('Error getting current location:', err);
    chatHeader.textContent = `${recipient} – data unavailable`;
    return;
  }

  // 9. Compute distance and country in parallel
  try {
    const [km, country] = await Promise.all([
      getDrivingDistance(pos.latitude, pos.longitude, current.lat, current.lon),
      getCountryFromCoords(pos.latitude, pos.longitude)
    ]);

    const distText = (typeof km === 'number') ? `${km.toFixed(1)} km` : '–';
    const countryText = country || 'Unknown';
    chatHeader.textContent = `${recipient} 📍 ${distText} — ${countryText}`;
  } catch (err) {
    console.error('Error computing distance/country:', err);
    chatHeader.textContent = `${recipient} – data unavailable`;
  }
});





function onSignIn(googleUser) {
  var profile = googleUser.getBasicProfile();
  console.log('ID: ' + profile.getId());
  console.log('Name: ' + profile.getName());
  console.log('Image URL: ' + profile.getImageUrl());
  console.log('Email: ' + profile.getEmail());
}

async function saveCoordinates(lat, lon, username) {
  const url = `/loginPage/api/position/${lat}/${lon}/${username}`;
  const resp = await fetch(url); // defaults to GET
  if (!resp.ok) throw new Error(`Error ${resp.status}`);
}

async function updateOnlineUser(new_username, new_password, new_email) {
  const url = `/loginPage/api/updateOnlineUser/${new_username}/${new_password}/${new_email}`;
  const resp = await fetch(url); // defaults to GET
  if (!resp.ok) throw new Error(`Error ${resp.status}`);
}

async function updateCoordinates(lat, lon, username) {
  const url = `/loginPage/api/updatePosition/${lat}/${lon}/${username}`;
  const resp = await fetch(url); // defaults to GET
  if (!resp.ok) throw new Error(`Error ${resp.status}`);
}

async function updateUserAndCoordinates(new_username, lat, lon, old_username) {
  const resp_2 = await fetch(`/loginPage/api/updateUserAndCoordinates/${new_username}/${lat}/${lon}/${old_username}`);
  if (!resp_2.ok) throw new Error(`Error ${resp_2.status}`);
}


async function connect(event) {
    event.preventDefault();
    username = document.querySelector('#name').value.trim();
    password = document.querySelector('#password').value.trim();
    email = document.querySelector('#email').value.trim();
//    getUserPositions();
     try {
        const users = await getJson('/loginPage/api/users');

        const exists = users.some(user => user.username === username);
        const { lat, lon } = await getCurrentLocation();
        if (!exists) {
          await saveCoordinates(lat, lon, username);
        } else {
          await updateCoordinates(lat, lon, username);
        }
      } catch (err) {
        console.error('Error in checkAndSave:', err);
      }

    console.log({
       username,
       password,
       email
     });
     try{
   if (username && password && email ) {
       usernamePage.classList.add('hidden');
       chatPage.classList.remove('hidden');
       friendPage.classList.remove('hidden');
       initProfileModal();
       var socket = new SockJS('/ws');
       stompClient = Stomp.over(socket);
       stompClient.connect({}, onConnected, onError);
    } else {
        console.log("Please fill in all fields.");
    }
    }
    catch(error)
    {
    console.log(error);
    }
}

function onConnected() {
    stompClient.subscribe('/user/' + username + '/queue/receive', onMessageReceived);


    updateFriendArea();
    sendOldMessages();
//    while (friendArea.firstChild) {
//        friendArea.removeChild(friendArea.firstChild);
//      }

    stompClient.send("/app/chat.addUser", {}, JSON.stringify({
        sender: username,
        senderPassword: password,
        senderEmail: email,
        recipient: recipient,
        type: 'JOIN'
    }));

    connectingElement.classList.add('hidden');
}




function onError(error) {
    connectingElement.textContent = 'Could not connect to WebSocket server. Please refresh this page to try again!';
    connectingElement.style.color = 'red';
}

function sendMessage(event) {
    event.preventDefault();

    var messageContent = messageInput.value.trim();
    if (messageContent && stompClient) {
        var chatMessage = {
            sender: username,
            content: messageContent,
            recipient: recipient,
            type: 'CHAT'
        };

        stompClient.send("/app/chat.sendPrivateMessage/" + recipient, {}, JSON.stringify(chatMessage));
        stompClient.send("/app/chat.sendPrivateMessage/" + username, {}, JSON.stringify(chatMessage));
        messageInput.value = '';
    }
}


async function getJson(url) {
  const res = await fetch(url, { headers: { 'Accept': 'application/json' } });
  if (!res.ok) throw new Error(`Failed to fetch ${url}: ${res.status} ${res.statusText}`);
  return res.json();
}

async function renderFriend(user, onlineSet) {
  if (user.username === username) return;

  const li = document.createElement('li');
  li.classList.add('friend-message');
  li.style.backgroundColor = 'white';


  const av = document.createElement('i');
  if(file && username)
  {
  const objectURL = URL.createObjectURL(file);
  av.style.backgroundImage = `url(${objectURL})`;
  }
  else
  {
    av.style.backgroundColor = getAvatarColor(user.username);
  }

  const p = document.createElement('p');
  p.style.backgroundColor =onlineSet.has(user.username) ? '#07ba36' : '#6f706f';
  av.appendChild(p);
  li.appendChild(av);

  const name = document.createElement('span');
  name.textContent = ' ' + user.username;
  li.appendChild(name);


  friendArea.appendChild(li);

}

async function updateFriendArea() {
  try {

    const savedScroll = friendArea.scrollTop;

    friendArea.style.overflowY = 'hidden';

    friendArea.innerHTML = '';

    const [users, onlineUsersArr] = await Promise.all([
      getJson('/loginPage/api/users'),
      getJson('/loginPage/api/onlineUsers')
    ]);
    const onlineSet = new Set(onlineUsersArr.map(u => u.username));
    users.forEach(u => renderFriend(u, onlineSet));

    friendArea.scrollTop = savedScroll;
    friendArea.style.overflowY = 'auto';
  }
  catch(err) {
    console.error('updateFriendArea failed:', err);
  }
}

const timer = setInterval(updateFriendArea, 10000);


async function sendOldMessages() {
  try {

    friendPage.classList.remove('hidden');


    var sender_recipient = username+recipient;
    console.log('USERRRRRRRRRRRRR', sender_recipient)
    const msgsRes = await fetch("/api/messages/" + sender_recipient);
    if (!msgsRes.ok) throw new Error('Failed to fetch messages');
    const messages = await msgsRes.json();
    console.log('Historical messages:', messages);
    for (const m of messages) {
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

        messageElement.textContent = `${message.sender} has left!`;

        console.log('Online users: ', onlineUsers);
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

function getAvatarColor(messageSender) {
    var hash = 0;
    for (var i = 0; i < messageSender.length; i++) {
        hash = 31 * hash + messageSender.charCodeAt(i);
    }
    var index = Math.abs(hash % colors.length);
    return colors[index];
}
usernameForm.addEventListener('submit', connect);
messageForm.addEventListener('submit', sendMessage);





