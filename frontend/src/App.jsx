import {BrowserRouter, Route, Routes, useNavigate} from "react-router-dom";
import {useEffect} from "react";
import {AuthProvider} from "./security/AuthContext.jsx";
import Login from "./pages/Login.jsx";
import OrdersPage from "./pages/OrdersPage.jsx";
import CreateOrderPage from "./pages/CreateOrderPage.jsx";
import MobileNav from "./components/MobileNav.jsx";
import PrivateRoute from "./security/PrivateRoute.jsx";
import MainLayout from "./components/MainLayout.jsx";
//

const InitNavigation = ({children}) => {
    const navigate = useNavigate();

    useEffect(() => {
        history.navigate = navigate;
    }, [navigate]);

    return children;
};

function App() {

    return (
        <BrowserRouter>
            <AuthProvider>
                <InitNavigation>

                        <Routes>
                            <Route path="/" element={<Login/>}/>
                                <Route element={<MainLayout />}>
                                    <Route path="/orders" element={<OrdersPage />} />
                                    <Route path="/orders/create" element={<CreateOrderPage />} />
                                </Route>

                        </Routes>

                </InitNavigation>
            </AuthProvider>
        </BrowserRouter>
    )
}

export default App
