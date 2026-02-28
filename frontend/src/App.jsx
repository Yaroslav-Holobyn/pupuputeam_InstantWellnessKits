import {BrowserRouter, Route, Routes, useNavigate} from "react-router-dom";
import {useEffect} from "react";
import {AuthProvider} from "./security/AuthContext.jsx";
import Login from "./pages/Login.jsx";
import OrdersPage from "./pages/OrdersPage.jsx";
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
                        {/*<Toaster position="top-right"/>*/}

                        <Routes>
                            <Route path="/" element={<Login/>}/>
                            <Route path="/orders" element={<OrdersPage/>}/>



                            {/*    <Route path="/login" element={<Login/>}/>*/}

                        {/*    <Route element={<MainLayout/>}>*/}
                        {/*        <Route element={<PrivateRoute/>}>*/}
                        {/*            <Route path="/books" element={<BooksPage/>}/>*/}
                        {/*            <Route path="/books/:id" element={<BookPage />} />*/}
                        {/*            <Route path="/profile" element={<Profile/>}/>*/}
                        {/*            <Route path="/cart" element={<CartPage/>}/>*/}
                        {/*            <Route path="/orders" element={<MyOrdersPage />} />*/}

                        {/*        </Route>*/}

                        {/*        <Route element={<PrivateRoute onlyEmployee={true}/>}>*/}
                        {/*            <Route path="/books/:id/edit" element={<EditBookPage />} />*/}
                        {/*            <Route path="/admin" element={<AdminLayout />}>*/}
                        {/*                <Route path="users" element={<AdminUsers />} />*/}
                        {/*                <Route path="books" element={<AdminBooks />} />*/}
                        {/*                <Route path="orders" element={<AdminOrders />}/>*/}
                        {/*            </Route>*/}
                        {/*        </Route>*/}
                        {/*    </Route>*/}
                        {/*    <Route path="*" element={<NotFound />} />*/}
                        </Routes>
                    </InitNavigation>
            </AuthProvider>
        </BrowserRouter>
    )
}

export default App
