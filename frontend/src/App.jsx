import { useState, useEffect } from 'react'
import { AuthProvider, useAuth } from './context/AuthContext'
import Login from './components/Login'
import TrackForm from './components/TrackForm'
import ProductList from './components/ProductList'
import axios from 'axios'

function AppContent() {
    const { user, logout, getIdToken } = useAuth()
    const [products, setProducts] = useState([])
    const [loading, setLoading] = useState(false)

    const fetchProducts = async () => {
        setLoading(true)
        try {
            const token = await getIdToken()
            const response = await axios.get('/api/products', {
                headers: { Authorization: `Bearer ${token}` }
            })
            setProducts(response.data)
        } catch (error) {
            console.error('Error fetching products:', error)
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => {
        if (user) {
            fetchProducts()
        }
    }, [user])

    const handleProductAdded = () => {
        fetchProducts()
    }

    if (!user) {
        return <Login />
    }

    return (
        <div className="app">
            {/* Navbar */}
            <nav className="navbar-custom">
                <div className="container d-flex justify-content-between align-items-center">
                    <span className="navbar-brand-custom">
                        <span className="emoji">🐕</span> Price Drop Watchdog
                    </span>
                    <div className="nav-user">
                        <span className="nav-email">{user.email}</span>
                        <button className="btn-logout" onClick={logout}>Logout</button>
                    </div>
                </div>
            </nav>

            {/* Hero */}
            <section className="hero-section">
                <h1 className="hero-title">
                    Never Miss a <span className="gradient-text">Price Drop</span> Again
                </h1>
                <p className="hero-subtitle">
                    Track product prices from your favorite e-commerce stores. Get instant email alerts when prices fall below your target.
                </p>
            </section>

            {/* Main Content */}
            <div className="container">
                <div className="row g-4 justify-content-center">
                    <div className="col-lg-5">
                        <TrackForm onProductAdded={handleProductAdded} getIdToken={getIdToken} userEmail={user.email} />
                    </div>
                    <div className="col-lg-7">
                        <ProductList products={products} loading={loading} />
                    </div>
                </div>
            </div>

            {/* Footer */}
            <footer className="footer-custom">
                <div className="container">
                    <p>🐕 Price Drop Watchdog &middot; Built with React & Spring Boot</p>
                </div>
            </footer>
        </div>
    )
}

function App() {
    return (
        <AuthProvider>
            <AppContent />
        </AuthProvider>
    )
}

export default App
