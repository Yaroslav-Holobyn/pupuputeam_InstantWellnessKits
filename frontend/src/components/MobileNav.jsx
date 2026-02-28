import React from "react";
import { useNavigate, useLocation } from "react-router-dom";

const MobileNav = () => {
    const navigate = useNavigate();
    const location = useLocation();

    const isActive = (path) => location.pathname === path;

    return (
        <div style={styles.navContainer}>
            <button
                style={isActive("/orders") ? styles.activeNavBtn : styles.navBtn}
                onClick={() => navigate("/orders")}
            >
                <span style={styles.text}>Orders</span>
            </button>

            <button
                style={isActive("/create") ? styles.activeNavBtn : styles.navBtn}
                onClick={() => navigate("orders/create")}
            >
                <span style={styles.text}>Add New</span>
            </button>
        </div>
    );
};

const styles = {
    navContainer: {
        position: "fixed",
        bottom: 0,
        left: 0,
        right: 0,
        height: "65px",
        backgroundColor: "white",
        borderTop: "1px solid #e0e0e0",
        display: "flex",
        justifyContent: "space-around",
        alignItems: "center",
        boxShadow: "0 -2px 10px rgba(0,0,0,0.05)",
        zIndex: 1000,
        paddingBottom: "env(safe-area-inset-bottom)",
    },
    navBtn: {
        flex: 1,
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        justifyContent: "center",
        background: "none",
        border: "none",
        color: "#999",
        cursor: "pointer",
        height: "100%",
        transition: "color 0.2s",
    },
    activeNavBtn: {
        flex: 1,
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        justifyContent: "center",
        background: "none",
        border: "none",
        color: "#5D4037",
        cursor: "pointer",
        height: "100%",
    },
    icon: {
        fontSize: "1.4rem",
        marginBottom: "2px",
    },
    text: {
        fontSize: "0.75rem",
        fontWeight: "600",
    }
};

export default MobileNav;