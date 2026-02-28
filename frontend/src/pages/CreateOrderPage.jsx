import React, { useState } from "react";
import OrderService from "../services/impl/OrderService";

const convertNyToUtcIso = (nyDateString) => {
    if (!nyDateString) return null;
    const [datePart, timePart] = nyDateString.split('T');
    const [year, month, day] = datePart.split('-');
    const [hour, minute] = timePart.split(':');

    const assumedUtcTime = Date.UTC(year, month - 1, day, hour, minute);
    const dateObj = new Date(assumedUtcTime);

    const nyTimeStr = dateObj.toLocaleString('en-US', { timeZone: 'America/New_York' });
    const utcTimeStr = dateObj.toLocaleString('en-US', { timeZone: 'UTC' });

    const offsetMs = new Date(utcTimeStr).getTime() - new Date(nyTimeStr).getTime();
    return new Date(assumedUtcTime + offsetMs).toISOString();
};

const CreateOrderPage = () => {
    const [formData, setFormData] = useState({
        subtotal: "",
        latitude: "",
        longitude: "",
        timestamp: ""
    });

    const [csvFile, setCsvFile] = useState(null);

    const [isCreating, setIsCreating] = useState(false);
    const [isImporting, setIsImporting] = useState(false);
    const [message, setMessage] = useState({ text: "", type: "" }); // type: "success" | "error"

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handleFileChange = (e) => {
        if (e.target.files && e.target.files.length > 0) {
            setCsvFile(e.target.files[0]);
        }
    };

    const handleCreateSubmit = async (e) => {
        e.preventDefault();
        setIsCreating(true);
        setMessage({ text: "", type: "" });

        try {
            const payload = {
                subtotal: parseFloat(formData.subtotal),
                latitude: parseFloat(formData.latitude),
                longitude: parseFloat(formData.longitude),
                timestamp: formData.timestamp ? convertNyToUtcIso(formData.timestamp) : null
            };

            await OrderService.create(payload);
            setMessage({ text: "Order created successfully!", type: "success" });

            setFormData({ subtotal: "", latitude: "", longitude: "", timestamp: "" });
        } catch (err) {
            setMessage({
                text: err.response?.data?.message || "Failed to create order. Please check your data.",
                type: "error"
            });
        } finally {
            setIsCreating(false);
        }
    };

    const handleImportSubmit = async (e) => {
        e.preventDefault();
        if (!csvFile) {
            setMessage({ text: "Please select a CSV file first.", type: "error" });
            return;
        }

        setIsImporting(true);
        setMessage({ text: "", type: "" });

        try {
            const response = await OrderService.importCsv(csvFile);
            const count = response.importedCount || response.data?.importedCount || "Multiple";
            setMessage({ text: `Successfully imported ${count} orders!`, type: "success" });

            setCsvFile(null);
            document.getElementById("csvFileInput").value = "";
        } catch (err) {
            setMessage({
                text: err.response?.data?.message || "Failed to import CSV.",
                type: "error"
            });
        } finally {
            setIsImporting(false);
        }
    };

    return (
        <div style={styles.container}>
            <h1 style={styles.pageTitle}>Add Orders</h1>

            {message.text && (
                <div style={message.type === "success" ? styles.successMessage : styles.errorMessage}>
                    {message.text}
                </div>
            )}

            <div style={styles.card}>
                <h2 style={styles.cardTitle}>Create Manually</h2>
                <form onSubmit={handleCreateSubmit} style={styles.form}>

                    <div style={styles.inputGroup}>
                        <label style={styles.label}>Subtotal ($)</label>
                        <input
                            type="number"
                            step="0.01"
                            name="subtotal"
                            value={formData.subtotal}
                            onChange={handleChange}
                            required
                            style={styles.input}
                            placeholder="e.g. 45.99"
                        />
                    </div>

                    <div style={styles.rowGrid}>
                        <div style={styles.inputGroup}>
                            <label style={styles.label}>Latitude</label>
                            <input
                                type="number"
                                step="any"
                                name="latitude"
                                value={formData.latitude}
                                onChange={handleChange}
                                required
                                style={styles.input}
                                placeholder="e.g. 40.7128"
                            />
                        </div>
                        <div style={styles.inputGroup}>
                            <label style={styles.label}>Longitude</label>
                            <input
                                type="number"
                                step="any"
                                name="longitude"
                                value={formData.longitude}
                                onChange={handleChange}
                                required
                                style={styles.input}
                                placeholder="e.g. -74.0060"
                            />
                        </div>
                    </div>

                    <div style={styles.inputGroup}>
                        <label style={styles.label}>Timestamp (NY Time)</label>
                        <input
                            type="datetime-local"
                            name="timestamp"
                            value={formData.timestamp}
                            onChange={handleChange}
                            required
                            style={styles.input}
                        />
                    </div>

                    <button
                        type="submit"
                        disabled={isCreating}
                        style={isCreating ? styles.buttonDisabled : styles.buttonPrimary}
                    >
                        {isCreating ? "Creating..." : "Create Order"}
                    </button>
                </form>
            </div>

            <div style={styles.dividerContainer}>
                <div style={styles.dividerLine}></div>
                <span style={styles.dividerText}>OR</span>
                <div style={styles.dividerLine}></div>
            </div>

            <div style={styles.card}>
                <h2 style={styles.cardTitle}>Import from CSV</h2>
                <form onSubmit={handleImportSubmit} style={styles.form}>

                    <div style={styles.inputGroup}>
                        <label style={styles.label}>Select CSV File</label>
                        <input
                            id="csvFileInput"
                            type="file"
                            accept=".csv"
                            onChange={handleFileChange}
                            style={styles.fileInput}
                        />
                    </div>

                    <button
                        type="submit"
                        disabled={isImporting || !csvFile}
                        style={(isImporting || !csvFile) ? styles.buttonDisabled : styles.buttonSecondary}
                    >
                        {isImporting ? "Importing..." : "Upload CSV"}
                    </button>
                </form>
            </div>
        </div>
    );
};

const styles = {
    container: {
        width: "100%",
        maxWidth: "500px",
        margin: "0 auto",
        padding: "clamp(15px, 4vw, 25px)",
        backgroundColor: "#f0f2f5",
        minHeight: "100vh",
        fontFamily: "'Segoe UI', Tahoma, Geneva, Verdana, sans-serif",
        boxSizing: "border-box",
    },
    pageTitle: {
        fontSize: "clamp(1.5rem, 6vw, 1.8rem)",
        color: "#1a1a1a",
        marginBottom: "1.5rem",
        textAlign: "center",
        fontWeight: "700",
    },
    card: {
        backgroundColor: "white",
        padding: "1.5rem",
        borderRadius: "12px",
        boxShadow: "0 4px 12px rgba(0,0,0,0.05)",
        boxSizing: "border-box",
    },
    cardTitle: {
        fontSize: "1.2rem",
        color: "#333",
        marginBottom: "1.2rem",
        marginTop: "0",
        fontWeight: "600",
    },
    form: {
        display: "flex",
        flexDirection: "column",
        gap: "1.2rem",
    },
    rowGrid: {
        display: "grid",
        gridTemplateColumns: "1fr 1fr",
        gap: "1rem",
    },
    inputGroup: {
        display: "flex",
        flexDirection: "column",
        gap: "0.4rem",
    },
    label: {
        fontSize: "0.85rem",
        color: "#555",
        fontWeight: "600",
    },
    input: {
        padding: "0.9rem",
        borderRadius: "8px",
        border: "1px solid #ccc",
        fontSize: "16px", // Запобігає зуму на iOS
        outline: "none",
        width: "100%",
        boxSizing: "border-box",
        appearance: "none",
        backgroundColor: "#fafafa",
        transition: "border-color 0.2s",
    },
    fileInput: {
        padding: "0.8rem",
        borderRadius: "8px",
        border: "1px dashed #aaa",
        fontSize: "16px",
        width: "100%",
        boxSizing: "border-box",
        backgroundColor: "#fafafa",
        cursor: "pointer",
    },
    buttonPrimary: {
        padding: "1rem",
        backgroundColor: "#5D4037",
        color: "white",
        border: "none",
        borderRadius: "8px",
        cursor: "pointer",
        fontWeight: "600",
        fontSize: "1rem",
        marginTop: "0.5rem",
        transition: "background-color 0.2s",
    },
    buttonSecondary: {
        padding: "1rem",
        backgroundColor: "#2e7d32", // Зелений для імпорту
        color: "white",
        border: "none",
        borderRadius: "8px",
        cursor: "pointer",
        fontWeight: "600",
        fontSize: "1rem",
        marginTop: "0.5rem",
        transition: "background-color 0.2s",
    },
    buttonDisabled: {
        padding: "1rem",
        backgroundColor: "#cccccc",
        color: "#666666",
        border: "none",
        borderRadius: "8px",
        cursor: "not-allowed",
        fontWeight: "600",
        fontSize: "1rem",
        marginTop: "0.5rem",
    },
    dividerContainer: {
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        margin: "1.5rem 0",
    },
    dividerLine: {
        flex: 1,
        height: "1px",
        backgroundColor: "#ddd",
    },
    dividerText: {
        margin: "0 1rem",
        color: "#888",
        fontWeight: "600",
        fontSize: "0.9rem",
    },
    successMessage: {
        backgroundColor: "#e8f5e9",
        color: "#2e7d32",
        padding: "1rem",
        borderRadius: "8px",
        marginBottom: "1.5rem",
        border: "1px solid #c8e6c9",
        textAlign: "center",
        fontWeight: "500",
    },
    errorMessage: {
        backgroundColor: "#ffebee",
        color: "#c62828",
        padding: "1rem",
        borderRadius: "8px",
        marginBottom: "1.5rem",
        border: "1px solid #ffcdd2",
        textAlign: "center",
        fontWeight: "500",
    }
};

export default CreateOrderPage;