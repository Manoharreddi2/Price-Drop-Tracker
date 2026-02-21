import { useState } from 'react'
import axios from 'axios'

function TrackForm({ onProductAdded, getIdToken, userEmail }) {
    const [formData, setFormData] = useState({
        productUrl: '',
        targetPrice: '',
        email: ''
    })
    const [loading, setLoading] = useState(false)
    const [message, setMessage] = useState(null)

    const handleChange = (e) => {
        setFormData({
            ...formData,
            [e.target.name]: e.target.value
        })
    }

    const handleSubmit = async (e) => {
        e.preventDefault()
        setLoading(true)
        setMessage(null)

        try {
            const token = await getIdToken()
            const payload = {
                productUrl: formData.productUrl,
                targetPrice: parseFloat(formData.targetPrice),
                email: formData.email || userEmail
            }

            const response = await axios.post('/api/track-product', payload, {
                headers: { Authorization: `Bearer ${token}` }
            })

            setMessage({ type: 'success', text: '✅ ' + response.data.message })
            setFormData({ productUrl: '', targetPrice: '', email: '' })

            if (onProductAdded) {
                onProductAdded()
            }
        } catch (error) {
            const errorMsg = error.response?.data?.error || 'Something went wrong. Please try again.'
            setMessage({ type: 'error', text: '❌ ' + errorMsg })
        } finally {
            setLoading(false)
        }
    }

    return (
        <div className="glass-card">
            <div className="card-header-custom">
                <div className="icon">🎯</div>
                <h3>Track a Product</h3>
            </div>

            <form onSubmit={handleSubmit}>
                <div className="form-group-custom">
                    <label className="form-label-custom" htmlFor="productUrl">Product URL</label>
                    <input
                        type="url"
                        id="productUrl"
                        name="productUrl"
                        className="form-control form-control-custom"
                        placeholder="https://www.example.com/product..."
                        value={formData.productUrl}
                        onChange={handleChange}
                        required
                    />
                </div>

                <div className="form-group-custom">
                    <label className="form-label-custom" htmlFor="targetPrice">Target Price (₹)</label>
                    <input
                        type="number"
                        id="targetPrice"
                        name="targetPrice"
                        className="form-control form-control-custom"
                        placeholder="Enter your target price"
                        min="1"
                        step="0.01"
                        value={formData.targetPrice}
                        onChange={handleChange}
                        required
                    />
                </div>

                <div className="form-group-custom">
                    <label className="form-label-custom" htmlFor="email">Email Address</label>
                    <input
                        type="email"
                        id="email"
                        name="email"
                        className="form-control form-control-custom"
                        placeholder={userEmail || 'your@email.com'}
                        value={formData.email}
                        onChange={handleChange}
                    />
                    <small className="text-muted-custom">Leave empty to use your account email</small>
                </div>

                <button
                    type="submit"
                    className="btn-primary-custom"
                    disabled={loading}
                >
                    {loading ? (
                        <>
                            <span className="spinner-custom"></span>
                            Tracking...
                        </>
                    ) : (
                        '🔔 Track Price'
                    )}
                </button>
            </form>

            {message && (
                <div className={`mt-3 ${message.type === 'success' ? 'alert-custom-success' : 'alert-custom-error'}`}>
                    {message.text}
                </div>
            )}
        </div>
    )
}

export default TrackForm
