import axios from "axios";
import Cookies from "js-cookie";
import {history} from "./history";

const HOST_URL = "http://localhost:8080";

export const client = axios.create({
    baseURL: HOST_URL,
    headers: {
        "Content-Type": "application/json",
    },
});

client.interceptors.request.use(
    config => {
        const token = Cookies.get("accessToken");
        if (token) {
            config.headers["Authorization"] = `Bearer ${token}`;
        }
        return config;
    },
    error => Promise.reject(error)
);

client.interceptors.response.use(
    response => response,
    async error => {
        const { response } = error;

        if (response && response.status === 401) {

            Cookies.remove("accessToken");

            if (history.navigate) {
                history.navigate("/login");
            } else {
                window.location.href = "/login";
            }
        }

        return Promise.reject(error);
    }
);