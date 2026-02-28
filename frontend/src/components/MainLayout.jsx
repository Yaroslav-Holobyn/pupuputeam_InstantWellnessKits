import React from "react";
import { Outlet } from "react-router-dom";
import MobileNav from "./MobileNav";

const MainLayout = () => {
    return (
        <div style={{ paddingBottom: "70px", minHeight: "100vh", backgroundColor: "#f0f2f5" }}>
            <Outlet />

            <MobileNav />
        </div>
    );
};

export default MainLayout;