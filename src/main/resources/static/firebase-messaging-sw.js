// Firebase 라이브러리 로드
importScripts('https://www.gstatic.com/firebasejs/10.8.0/firebase-app-compat.js');
importScripts('https://www.gstatic.com/firebasejs/10.8.0/firebase-messaging-compat.js');

// index.html에서 썼던 설정값과 똑같이 넣으세요
const firebaseConfig = {
    apiKey: "AIzaSyDW5Wn23NqESCbOdpCrKw-uphDjalwfdAo",
    authDomain: "nodingo-sogang.firebaseapp.com",
    projectId: "nodingo-sogang",
    storageBucket: "nodingo-sogang.firebasestorage.app",
    messagingSenderId: "527574334308",
    appId: "1:527574334308:web:ee462ff0e82eb2af599871"
};

firebase.initializeApp(firebaseConfig);
const messaging = firebase.messaging();

messaging.onBackgroundMessage((payload) => {
    console.log('[firebase-messaging-sw.js] 백그라운드 메시지 수신: ', payload);
    const notificationTitle = payload.notification.title;
    const notificationOptions = {
        body: payload.notification.body,
        icon: '/firebase-logo.png'
    };

    self.registration.showNotification(notificationTitle, notificationOptions);
});