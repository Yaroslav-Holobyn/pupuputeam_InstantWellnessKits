import {useState} from "react";
import {useNavigate} from "react-router-dom";
import {useAuth} from "../security/AuthContext.jsx";
import {extractErrorMessage} from "../utils/errorHandler.js";

const Login = () => {
    const { login } = useAuth();
    const navigate = useNavigate();

    const [formData, setFormData] = useState({
        username: "",
        password: ""
    });

    const [error, setError] = useState(null);
    const [isLoading, setIsLoading] = useState(false);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));

        if (error) setError(null);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setIsLoading(true);
        setError(null);

        try {
            await login(formData.username, formData.password);
            navigate("/orders");
        } catch (err) {
            const message = extractErrorMessage(err);
            setError(message);
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div style={styles.container}>
            <div style={styles.card}>
                <h2 style={styles.title}>Login</h2>

                <form onSubmit={handleSubmit} style={styles.form}>

                    <div style={styles.inputGroup}>
                        <label htmlFor="username" style={styles.label}>Username</label>
                        <input
                            type="text"
                            id="username"
                            name="username"
                            value={formData.username}
                            onChange={handleChange}
                            required
                            style={styles.input}
                            placeholder="Enter your username"
                        />
                    </div>

                    <div style={styles.inputGroup}>
                        <label htmlFor="password" style={styles.label}>Password</label>
                        <input
                            type="password"
                            id="password"
                            name="password"
                            value={formData.password}
                            onChange={handleChange}
                            required
                            style={styles.input}
                            placeholder="Enter your password"
                        />
                    </div>

                    {error && (
                        <div style={styles.errorMessage}>
                            {error}
                        </div>
                    )}

                    <button
                        type="submit"
                        disabled={isLoading}
                        style={isLoading ? styles.buttonDisabled : styles.button}
                    >
                        {isLoading ? "Logging in..." : "Login"}
                    </button>
                </form>
            </div>
        </div>
    );
};

const styles = {
    container: {
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        minHeight: "100vh",
        backgroundColor: "#f0f2f5",
        fontFamily: "'Segoe UI', Tahoma, Geneva, Verdana, sans-serif",
        padding: "1rem",
    },
    card: {
        backgroundColor: "white",
        padding: "clamp(1.5rem, 5vw, 2.5rem)",
        borderRadius: "12px",
        boxShadow: "0 8px 24px rgba(0,0,0,0.12)",
        width: "100%",
        maxWidth: "400px",
        boxSizing: "border-box",
    },
    title: {
        textAlign: "center",
        marginBottom: "1.5rem",
        color: "#1a1a1a",
        fontSize: "1.5rem",
        fontWeight: "600"
    },
    form: {
        display: "flex",
        flexDirection: "column",
        gap: "1rem",
    },
    inputGroup: {
        display: "flex",
        flexDirection: "column",
        gap: "0.4rem",
    },
    label: {
        fontSize: "0.85rem",
        fontWeight: "500",
        color: "#4a4a4a",
    },
    input: {
        padding: "0.9rem",
        borderRadius: "8px",
        border: "1px solid #ddd",
        fontSize: "16px",
        outline: "none",
        transition: "border-color 0.2s",
        appearance: "none",
    },
    button: {
        marginTop: "0.5rem",
        padding: "1rem",
        backgroundColor: "#5D4037",
        color: "white",
        border: "none",
        borderRadius: "8px",
        fontSize: "1rem",
        fontWeight: "600",
        cursor: "pointer",
        transition: "background-color 0.2s",
        WebkitTapHighlightColor: "transparent",
    },
    buttonDisabled: {
        marginTop: "0.5rem",
        padding: "1rem",
        backgroundColor: "#735951",
        color: "white",
        border: "none",
        borderRadius: "8px",
        fontSize: "1rem",
        cursor: "not-allowed",
    },
    errorMessage: {
        backgroundColor: "#fee2e2",
        color: "#dc2626",
        padding: "0.75rem",
        borderRadius: "6px",
        fontSize: "0.85rem",
        textAlign: "left",
        border: "1px solid #fecaca",
        whiteSpace: "pre-wrap",
        wordBreak: "break-word"
    }
};

export default Login;