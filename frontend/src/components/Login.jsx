import { useState } from 'react'
import { useAuth } from '../context/AuthContext'

function Login() {
    const { login, signup, loginWithGoogle } = useAuth()
    const [isSignup, setIsSignup] = useState(false)
    const [email, setEmail] = useState('')
    const [password, setPassword] = useState('')
    const [error, setError] = useState('')
    const [loading, setLoading] = useState(false)

    const handleSubmit = async (e) => {
        e.preventDefault()
        setError('')
        setLoading(true)

        try {
            if (isSignup) {
                await signup(email, password)
            } else {
                await login(email, password)
            }
        } catch (err) {
            const msg = err.code === 'auth/user-not-found' ? 'No account found with this email'
                : err.code === 'auth/wrong-password' ? 'Incorrect password'
                    : err.code === 'auth/email-already-in-use' ? 'Email already registered'
                        : err.code === 'auth/weak-password' ? 'Password must be at least 6 characters'
                            : err.code === 'auth/invalid-email' ? 'Invalid email address'
                                : err.code === 'auth/invalid-credential' ? 'Invalid email or password'
                                    : err.message
            setError(msg)
        } finally {
            setLoading(false)
        }
    }

    return (
        <div className="auth-page">
            <div className="auth-container">
                <div className="auth-header">
                    <span className="auth-logo">🐕</span>
                    <h1>Price Drop Watchdog</h1>
                    <p>{isSignup ? 'Create your account' : 'Sign in to your account'}</p>
                </div>

                <div className="glass-card">
                    <form onSubmit={handleSubmit}>
                        <div className="form-group-custom">
                            <label className="form-label-custom" htmlFor="auth-email">Email</label>
                            <input
                                type="email"
                                id="auth-email"
                                className="form-control form-control-custom"
                                placeholder="your@email.com"
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                                required
                            />
                        </div>

                        <div className="form-group-custom">
                            <label className="form-label-custom" htmlFor="auth-password">Password</label>
                            <input
                                type="password"
                                id="auth-password"
                                className="form-control form-control-custom"
                                placeholder="Enter your password"
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                required
                                minLength={6}
                            />
                        </div>

                        {error && (
                            <div className="alert-custom-error mb-3">
                                ❌ {error}
                            </div>
                        )}

                        <button
                            type="submit"
                            className="btn-primary-custom"
                            disabled={loading}
                        >
                            {loading ? (
                                <><span className="spinner-custom"></span>{isSignup ? 'Creating Account...' : 'Signing In...'}</>
                            ) : (
                                isSignup ? '🚀 Sign Up' : '🔓 Sign In'
                            )}
                        </button>
                    </form>

                    <div className="auth-divider">
                        <span>or</span>
                    </div>

                    <button
                        className="btn-google"
                        onClick={async () => {
                            try {
                                setError('')
                                await loginWithGoogle()
                            } catch (err) {
                                setError(err.message)
                            }
                        }}
                    >
                        <svg width="18" height="18" viewBox="0 0 48 48"><path fill="#EA4335" d="M24 9.5c3.54 0 6.71 1.22 9.21 3.6l6.85-6.85C35.9 2.38 30.47 0 24 0 14.62 0 6.51 5.38 2.56 13.22l7.98 6.19C12.43 13.72 17.74 9.5 24 9.5z" /><path fill="#4285F4" d="M46.98 24.55c0-1.57-.15-3.09-.38-4.55H24v9.02h12.94c-.58 2.96-2.26 5.48-4.78 7.18l7.73 6c4.51-4.18 7.09-10.36 7.09-17.65z" /><path fill="#FBBC05" d="M10.53 28.59c-.48-1.45-.76-2.99-.76-4.59s.27-3.14.76-4.59l-7.98-6.19C.92 16.46 0 20.12 0 24c0 3.88.92 7.54 2.56 10.78l7.97-6.19z" /><path fill="#34A853" d="M24 48c6.48 0 11.93-2.13 15.89-5.81l-7.73-6c-2.15 1.45-4.92 2.3-8.16 2.3-6.26 0-11.57-4.22-13.47-9.91l-7.98 6.19C6.51 42.62 14.62 48 24 48z" /></svg>
                        Continue with Google
                    </button>

                    <div className="auth-toggle">
                        <p>
                            {isSignup ? 'Already have an account?' : "Don't have an account?"}{' '}
                            <button
                                className="auth-toggle-btn"
                                onClick={() => { setIsSignup(!isSignup); setError('') }}
                            >
                                {isSignup ? 'Sign In' : 'Sign Up'}
                            </button>
                        </p>
                    </div>
                </div>
            </div>
        </div>
    )
}

export default Login
