import {Navigate, Outlet} from "react-router-dom";
import {useAuth} from "./AuthContext.jsx";

const PrivateRoute = ({ onlyEmployee = false }) => {
    const { isAuthenticated, isEmployee } = useAuth();

    if (!isAuthenticated) {
        return <Navigate to="/login" replace />;
    }

    if (onlyEmployee && !isEmployee) {
        return <Navigate to="/" replace />;
    }

    return <Outlet />;
};

export default PrivateRoute;