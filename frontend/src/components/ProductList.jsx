function ProductList({ products = [], loading }) {
    return (
        <div className="glass-card">
            <div className="card-header-custom">
                <div className="icon">📦</div>
                <h3>Tracked Products</h3>
            </div>

            {loading ? (
                <div className="empty-state">
                    <span className="spinner-custom" style={{ width: 32, height: 32 }}></span>
                    <p className="mt-3">Loading products...</p>
                </div>
            ) : products.length === 0 ? (
                <div className="empty-state">
                    <span className="emoji-large">🔍</span>
                    <p>No products being tracked yet.<br />Add your first product to get started!</p>
                </div>
            ) : (
                <div className="table-container">
                    <table className="table-custom">
                        <thead>
                            <tr>
                                <th>Product URL</th>
                                <th>Target (₹)</th>
                                <th>Current (₹)</th>
                                <th>Last Checked</th>
                                <th>Status</th>
                            </tr>
                        </thead>
                        <tbody>
                            {products.map((product, index) => (
                                <tr key={product.id || index}>
                                    <td className="url-cell">
                                        <a href={product.productUrl} target="_blank" rel="noopener noreferrer" title={product.productUrl}>
                                            {product.productUrl}
                                        </a>
                                    </td>
                                    <td className="price-cell">₹{product.targetPrice?.toLocaleString('en-IN') || '—'}</td>
                                    <td className="price-cell">
                                        {product.currentPrice > 0 ? `₹${product.currentPrice.toLocaleString('en-IN')}` : '—'}
                                    </td>
                                    <td>{product.lastChecked || '—'}</td>
                                    <td>
                                        {product.notified ? (
                                            <span className="badge-notified">✅ Notified</span>
                                        ) : (
                                            <span className="badge-watching">👀 Watching</span>
                                        )}
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            )}
        </div>
    )
}

export default ProductList
