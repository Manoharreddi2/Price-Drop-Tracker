import { createContext, useContext, useState, useEffect } from 'react'
import {
    onAuthStateChanged,
    signInWithEmailAndPassword,
    createUserWithEmailAndPassword,
    signOut,
    GoogleAuthProvider,
    signInWithPopup
} from 'firebase/auth'
import { auth } from '../firebase'

const AuthContext = createContext()

export function useAuth() {
    return useContext(AuthContext)
}

export function AuthProvider({ children }) {
    const [user, setUser] = useState(null)
    const [loading, setLoading] = useState(true)

    useEffect(() => {
        const unsubscribe = onAuthStateChanged(auth, (currentUser) => {
            setUser(currentUser)
            setLoading(false)
        })
        return unsubscribe
    }, [])

    const signup = (email, password) => {
        return createUserWithEmailAndPassword(auth, email, password)
    }

    const login = (email, password) => {
        return signInWithEmailAndPassword(auth, email, password)
    }

    const logout = () => {
        return signOut(auth)
    }

    const loginWithGoogle = () => {
        const provider = new GoogleAuthProvider()
        return signInWithPopup(auth, provider)
    }

    const getIdToken = async () => {
        if (user) {
            return await user.getIdToken()
        }
        return null
    }

    const value = { user, loading, signup, login, logout, loginWithGoogle, getIdToken }

    return (
        <AuthContext.Provider value={value}>
            {!loading && children}
        </AuthContext.Provider>
    )
}
