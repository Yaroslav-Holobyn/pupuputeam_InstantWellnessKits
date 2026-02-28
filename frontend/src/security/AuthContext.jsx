import {createContext, useContext, useState} from "react";
import Cookies from "js-cookie";
import {client} from "../utils/client.js";

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
    const [isLoading, setIsLoading] = useState(false);
    const [isAuthenticated, setIsAuthenticated] = useState(false);

    const login = async (username, password) => {
        try {
            setIsLoading(true);
            const response = await client.post("/auth/login", {email: username, password});

            const token = response.data.accessToken || response.data.token;

            if (token) {
                Cookies.set("accessToken", token);
                setIsAuthenticated(true)
                return true;
            }
            // eslint-disable-next-line no-unused-vars
        }catch (e){
            setIsAuthenticated(false)
        }finally {
            setIsLoading(false);
        }
    };

    const logout = () => {
        Cookies.remove("accessToken");
        setIsAuthenticated(false)
        window.location.href = "/";
    };

    return (
        <AuthContext.Provider value={{
            login,
            logout,
            isLoading,
            isAuthenticated
        }}>
            {!isLoading && children}
        </AuthContext.Provider>
    );
};

// eslint-disable-next-line react-refresh/only-export-components
export const useAuth = () => useContext(AuthContext);