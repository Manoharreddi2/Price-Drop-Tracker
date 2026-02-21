import { initializeApp } from 'firebase/app'
import { getAuth } from 'firebase/auth'

const firebaseConfig = {
    apiKey: import.meta.env.VITE_FIREBASE_API_KEY || "AIzaSyDF0oqmC58mqp47lMiBRNfHz1-W-0PHuvk",
    authDomain: import.meta.env.VITE_FIREBASE_AUTH_DOMAIN || "price-drop-tracker-c1489.firebaseapp.com",
    projectId: import.meta.env.VITE_FIREBASE_PROJECT_ID || "price-drop-tracker-c1489",
    storageBucket: import.meta.env.VITE_FIREBASE_STORAGE_BUCKET || "price-drop-tracker-c1489.firebasestorage.app",
    messagingSenderId: import.meta.env.VITE_FIREBASE_MESSAGING_SENDER_ID || "386283938039",
    appId: import.meta.env.VITE_FIREBASE_APP_ID || "1:386283938039:web:26e34a18bef03e0b76f3f2",
    measurementId: import.meta.env.VITE_FIREBASE_MEASUREMENT_ID || "G-KPJ942E09G"
}

const app = initializeApp(firebaseConfig)
export const auth = getAuth(app)
export default app
