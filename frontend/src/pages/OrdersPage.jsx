import React, { useState, useEffect } from "react";
import OrderService from "../services/impl/OrderService";

const OrdersPage = () => {
    const [orders, setOrders] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    const [page, setPage] = useState(1);
    const [totalPages, setTotalPages] = useState(1);

    const [showFilters, setShowFilters] = useState(false);
    const [expandedOrderId, setExpandedOrderId] = useState(null);

    const [filterInput, setFilterInput] = useState({
        minAmount: "", maxAmount: "", countyName: "", muniName: "",
        minTaxRate: "", maxTaxRate: "", startTime: "", endTime: ""
    });

    const [appliedFilters, setAppliedFilters] = useState({});

    const fetchOrders = async () => {
        setLoading(true);
        setError(null);
        try {
            const cleanFilters = Object.fromEntries(
                Object.entries(appliedFilters).filter(([, v]) => v !== "")
            );

            const response = await OrderService.getAll(cleanFilters, page - 1, 12);

            setOrders(response.content || []);
            setTotalPages(response.totalPages || 1);
        } catch (err) {
            setError(err.response?.data?.message || "Error loading orders");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchOrders();
    }, [page, appliedFilters]);

    const handleFilterChange = (e) => {
        const { name, value } = e.target;
        setFilterInput((prev) => ({ ...prev, [name]: value }));
    };

    const applyFilters = (e) => {
        e.preventDefault();
        setPage(1);

        const processedFilters = { ...filterInput };

        if (processedFilters.startTime) {
            processedFilters.startTime = new Date(processedFilters.startTime).toISOString();
        }
        if (processedFilters.endTime) {
            processedFilters.endTime = new Date(processedFilters.endTime).toISOString();
        }

        setAppliedFilters(processedFilters);
        setShowFilters(false);
    };

    const resetFilters = () => {
        const empty = {
            minAmount: "", maxAmount: "", countyName: "", muniName: "",
            minTaxRate: "", maxTaxRate: "", startTime: "", endTime: ""
        };
        setFilterInput(empty);
        setAppliedFilters({});
        setPage(1);
    };

    const toggleOrderDetails = (id) => {
        setExpandedOrderId(expandedOrderId === id ? null : id);
    };

    return (
        <div style={styles.container}>
            <div style={styles.header}>
                <h1 style={styles.pageTitle}>Orders</h1>
                <button
                    style={styles.filterToggleButton}
                    onClick={() => setShowFilters(!showFilters)}
                >
                    {showFilters ? "Hide Filters" : "Show Filters"}
                </button>
            </div>

            {showFilters && (
                <div style={styles.filterCard}>
                    <form onSubmit={applyFilters} style={styles.formGrid}>
                        <div style={styles.inputGroup}>
                            <label style={styles.label}>Min Amount ($)</label>
                            <input type="number" step="0.01" name="minAmount" value={filterInput.minAmount} onChange={handleFilterChange} style={styles.input} />
                        </div>
                        <div style={styles.inputGroup}>
                            <label style={styles.label}>Max Amount ($)</label>
                            <input type="number" step="0.01" name="maxAmount" value={filterInput.maxAmount} onChange={handleFilterChange} style={styles.input} />
                        </div>

                        <div style={styles.inputGroupFull}>
                            <label style={styles.label}>From (Date/Time)</label>
                            <input type="datetime-local" name="startTime" value={filterInput.startTime} onChange={handleFilterChange} style={styles.input} />
                        </div>
                        <div style={styles.inputGroupFull}>
                            <label style={styles.label}>To (Date/Time)</label>
                            <input type="datetime-local" name="endTime" value={filterInput.endTime} onChange={handleFilterChange} style={styles.input} />
                        </div>

                        <div style={styles.inputGroup}>
                            <label style={styles.label}>County</label>
                            <input type="text" name="countyName" value={filterInput.countyName} onChange={handleFilterChange} style={styles.input} placeholder="e.g. Erie" />
                        </div>
                        <div style={styles.inputGroup}>
                            <label style={styles.label}>Municipality</label>
                            <input type="text" name="muniName" value={filterInput.muniName} onChange={handleFilterChange} style={styles.input} placeholder="e.g. Buffalo" />
                        </div>

                        <div style={styles.inputGroup}>
                            <label style={styles.label}>Min Tax Rate (0-1.0)</label>
                            <input type="number" step="0.001" min="0" max="1" name="minTaxRate" value={filterInput.minTaxRate} onChange={handleFilterChange} style={styles.input} placeholder="0.04" />
                        </div>
                        <div style={styles.inputGroup}>
                            <label style={styles.label}>Max Tax Rate (0-1.0)</label>
                            <input type="number" step="0.001" min="0" max="1" name="maxTaxRate" value={filterInput.maxTaxRate} onChange={handleFilterChange} style={styles.input} placeholder="0.08" />
                        </div>

                        <div style={styles.buttonGroup}>
                            <button type="button" onClick={resetFilters} style={styles.resetButton}>Reset</button>
                            <button type="submit" style={styles.applyButton}>Apply</button>
                        </div>
                    </form>
                </div>
            )}

            {error && <div style={styles.errorMessage}>{error}</div>}
            {loading && <div style={styles.loadingMessage}>Loading...</div>}
            {!loading && orders.length === 0 && !error && (
                <div style={styles.emptyMessage}>No orders found</div>
            )}

            {!loading && orders.length > 0 && (
                <div style={styles.orderList}>
                    {orders.map((order) => (
                        <div key={order.id} style={styles.orderCard}>
                            <div style={styles.cardHeader}>
                                <span style={styles.orderId}>#{order.id}</span>
                                <span style={styles.orderDate}>{new Date(order.ts).toLocaleString()}</span>
                            </div>

                            <div style={styles.orderAmount}>
                                ${order.totalAmount?.toFixed(2) || "0.00"}
                            </div>

                            <div style={styles.orderLocation}>
                                <strong>Location:</strong> {order.countyName || "-"}, {order.muniName || "-"}
                            </div>

                            <button
                                onClick={() => toggleOrderDetails(order.id)}
                                style={styles.detailsToggle}
                            >
                                {expandedOrderId === order.id ? "Hide Details" : "Show All Details"}
                            </button>

                            {expandedOrderId === order.id && (
                                <div style={styles.expandedDetails}>
                                    <div style={styles.detailGrid}>
                                        <div>
                                            <div style={styles.detailLabel}>Subtotal</div>
                                            <div style={styles.detailValue}>${order.subtotal}</div>
                                        </div>
                                        <div>
                                            <div style={styles.detailLabel}>Tax Amount</div>
                                            <div style={styles.detailValue}>${order.taxAmount}</div>
                                        </div>
                                        <div>
                                            <div style={styles.detailLabel}>Composite Rate</div>
                                            <div style={styles.detailValue}>{(order.compositeTaxRate * 100).toFixed(2)}%</div>
                                        </div>
                                        <div>
                                            <div style={styles.detailLabel}>Muni Type</div>
                                            <div style={styles.detailValue}>{order.muniType || "-"}</div>
                                        </div>
                                    </div>

                                    <div style={styles.detailRow}>
                                        <div style={styles.detailLabel}>Coordinates</div>
                                        <div style={styles.detailValue}>{order.lat}, {order.lon}</div>
                                    </div>

                                    <div style={styles.chipsContainer}>
                                        {order.inNy && <span style={styles.chipSuccess}>NY</span>}
                                        {order.inMctd && <span style={styles.chipInfo}>MCTD</span>}
                                    </div>

                                    {order.jurisdictions?.length > 0 && (
                                        <div style={styles.jurisdictionsSection}>
                                            <div style={styles.detailLabel}>Jurisdictions</div>
                                            {order.jurisdictions.map((jur, idx) => (
                                                <div key={idx} style={styles.jurisdictionItem}>
                                                    <span>{jur.name} ({jur.type})</span>
                                                    <strong>{(jur.rate * 100).toFixed(2)}%</strong>
                                                </div>
                                            ))}
                                        </div>
                                    )}
                                </div>
                            )}
                        </div>
                    ))}
                </div>
            )}

            {totalPages > 1 && !loading && (
                <div style={styles.pagination}>
                    <button
                        style={page === 1 ? styles.pageBtnDisabled : styles.pageBtn}
                        onClick={() => setPage(p => Math.max(1, p - 1))}
                        disabled={page === 1}
                    >
                        Prev
                    </button>
                    <span style={styles.pageInfo}>Page {page} of {totalPages}</span>
                    <button
                        style={page === totalPages ? styles.pageBtnDisabled : styles.pageBtn}
                        onClick={() => setPage(p => Math.min(totalPages, p + 1))}
                        disabled={page === totalPages}
                    >
                        Next
                    </button>
                </div>
            )}
        </div>
    );
};

const styles = {
    container: {
        width: "100%",
        maxWidth: "600px",
        margin: "0 auto",
        padding: "clamp(10px, 3vw, 20px)",
        backgroundColor: "#f0f2f5",
        minHeight: "100vh",
        fontFamily: "'Segoe UI', Tahoma, Geneva, Verdana, sans-serif",
        boxSizing: "border-box",
    },
    header: {
        display: "flex",
        justifyContent: "space-between",
        alignItems: "center",
        marginBottom: "1rem",
    },
    pageTitle: {
        fontSize: "clamp(1.2rem, 5vw, 1.5rem)",
        color: "#1a1a1a",
        margin: 0,
    },
    filterToggleButton: {
        padding: "0.5rem 1rem",
        backgroundColor: "white",
        border: "1px solid #ddd",
        borderRadius: "8px",
        fontSize: "0.9rem",
        cursor: "pointer",
        fontWeight: "500",
        boxShadow: "0 1px 3px rgba(0,0,0,0.05)",
    },
    filterCard: {
        backgroundColor: "white",
        padding: "1rem",
        borderRadius: "12px",
        boxShadow: "0 2px 8px rgba(0,0,0,0.05)",
        marginBottom: "1.5rem",
        boxSizing: "border-box",
    },
    formGrid: {
        display: "grid",
        gridTemplateColumns: "repeat(auto-fit, minmax(140px, 1fr))",
        gap: "1rem",
    },
    inputGroup: {
        display: "flex",
        flexDirection: "column",
        gap: "0.4rem",
    },
    inputGroupFull: {
        display: "flex",
        flexDirection: "column",
        gap: "0.4rem",
        gridColumn: "1 / -1",
    },
    label: {
        fontSize: "0.85rem",
        color: "#4a4a4a",
        fontWeight: "500",
    },
    input: {
        padding: "0.8rem",
        borderRadius: "8px",
        border: "1px solid #ccc",
        fontSize: "16px",
        outline: "none",
        width: "100%",
        boxSizing: "border-box",
        appearance: "none",
    },
    buttonGroup: {
        gridColumn: "1 / -1",
        display: "flex",
        gap: "1rem",
        marginTop: "0.5rem",
    },
    resetButton: {
        flex: 1,
        padding: "0.9rem",
        backgroundColor: "white",
        color: "#5D4037",
        border: "1px solid #5D4037",
        borderRadius: "8px",
        cursor: "pointer",
        fontWeight: "600",
        fontSize: "1rem",
    },
    applyButton: {
        flex: 1,
        padding: "0.9rem",
        backgroundColor: "#5D4037",
        color: "white",
        border: "none",
        borderRadius: "8px",
        cursor: "pointer",
        fontWeight: "600",
        fontSize: "1rem",
    },
    orderList: {
        display: "flex",
        flexDirection: "column",
        gap: "1rem",
    },
    orderCard: {
        backgroundColor: "white",
        padding: "1rem",
        borderRadius: "12px",
        boxShadow: "0 2px 8px rgba(0,0,0,0.05)",
        boxSizing: "border-box",
    },
    cardHeader: {
        display: "flex",
        justifyContent: "space-between",
        marginBottom: "0.5rem",
    },
    orderId: {
        color: "#666",
        fontSize: "0.9rem",
        fontWeight: "500",
    },
    orderDate: {
        color: "#666",
        fontSize: "0.85rem",
    },
    orderAmount: {
        fontSize: "1.5rem",
        fontWeight: "bold",
        color: "#5D4037",
        marginBottom: "0.5rem",
    },
    orderLocation: {
        fontSize: "0.9rem",
        color: "#333",
        marginBottom: "1rem",
    },
    detailsToggle: {
        background: "none",
        border: "none",
        color: "#5D4037",
        padding: "0.5rem 0", // Larger touch area
        fontSize: "0.9rem",
        cursor: "pointer",
        fontWeight: "600",
        textDecoration: "underline",
        width: "100%", // Easy to tap
        textAlign: "left",
    },
    expandedDetails: {
        marginTop: "0.5rem",
        paddingTop: "1rem",
        borderTop: "1px solid #eee",
    },
    detailGrid: {
        display: "grid",
        gridTemplateColumns: "1fr 1fr",
        gap: "1rem",
        marginBottom: "1rem",
    },
    detailRow: {
        marginBottom: "1rem",
    },
    detailLabel: {
        fontSize: "0.75rem",
        color: "#888",
        marginBottom: "0.2rem",
        textTransform: "uppercase",
        letterSpacing: "0.5px",
    },
    detailValue: {
        fontSize: "0.95rem",
        color: "#222",
        fontWeight: "500",
    },
    chipsContainer: {
        display: "flex",
        gap: "0.5rem",
        marginBottom: "1rem",
        flexWrap: "wrap",
    },
    chipSuccess: {
        fontSize: "0.75rem",
        padding: "0.3rem 0.8rem",
        borderRadius: "16px",
        backgroundColor: "#e8f5e9",
        color: "#2e7d32",
        fontWeight: "600",
    },
    chipInfo: {
        fontSize: "0.75rem",
        padding: "0.3rem 0.8rem",
        borderRadius: "16px",
        backgroundColor: "#e3f2fd",
        color: "#1565c0",
        fontWeight: "600",
    },
    jurisdictionsSection: {
        marginTop: "1rem",
        backgroundColor: "#fafafa",
        padding: "0.8rem",
        borderRadius: "8px",
    },
    jurisdictionItem: {
        display: "flex",
        justifyContent: "space-between",
        padding: "0.4rem 0",
        borderBottom: "1px solid #eee",
        fontSize: "0.85rem",
    },
    pagination: {
        display: "flex",
        justifyContent: "space-between",
        alignItems: "center",
        marginTop: "2rem",
        padding: "0 0.5rem",
    },
    pageBtn: {
        padding: "0.8rem 1.2rem",
        backgroundColor: "white",
        border: "1px solid #ccc",
        borderRadius: "8px",
        cursor: "pointer",
        color: "#333",
        fontWeight: "600",
        boxShadow: "0 1px 2px rgba(0,0,0,0.05)",
    },
    pageBtnDisabled: {
        padding: "0.8rem 1.2rem",
        backgroundColor: "#f9f9f9",
        border: "1px solid #eee",
        borderRadius: "8px",
        cursor: "not-allowed",
        color: "#bbb",
        fontWeight: "600",
    },
    pageInfo: {
        fontSize: "0.9rem",
        color: "#555",
        fontWeight: "500",
    },
    errorMessage: {
        backgroundColor: "#fee2e2",
        color: "#dc2626",
        padding: "1rem",
        borderRadius: "8px",
        marginBottom: "1rem",
        border: "1px solid #fecaca",
    },
    loadingMessage: {
        textAlign: "center",
        padding: "3rem",
        color: "#666",
        fontWeight: "500",
    },
    emptyMessage: {
        textAlign: "center",
        padding: "3rem",
        color: "#666",
        backgroundColor: "white",
        borderRadius: "12px",
        border: "1px dashed #ccc",
    }
};

export default OrdersPage;