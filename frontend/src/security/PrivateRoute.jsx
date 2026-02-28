import {Navigate, Outlet} from "react-router-dom";
import {useAuth} from "./AuthContext.jsx";

const PrivateRoute = () => {
    const { isAuthenticated } = useAuth();

    if (!isAuthenticated) {
        return <Navigate to="/" replace />;
    }

    return <Outlet />;
};

export default PrivateRoute;