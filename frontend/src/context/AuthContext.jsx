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
        try {
            const unsubscribe = onAuthStateChanged(auth, (currentUser) => {
                setUser(currentUser)
                setLoading(false)
            }, (error) => {
                console.error('Auth state error:', error)
                setLoading(false)
            })
            return unsubscribe
        } catch (error) {
            console.error('Firebase init error:', error)
            setLoading(false)
        }
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
            {loading ? (
                <div className="auth-page">
                    <div className="text-center">
                        <span className="spinner-custom" style={{ width: 40, height: 40 }}></span>
                        <p className="mt-3">Dog-watching prices for you...</p>
                    </div>
                </div>
            ) : children}
        </AuthContext.Provider>
    )
}
