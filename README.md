# 🚀pupuputeam_InstantWellnessKits
## 🛠 Technology Stack
**Language:** Java 21  
**Framework:** Spring Boot 4.0.3  
**Database Migration:** Flyway  
**Security:** Spring Security (JWT)  
**Frontend:**
- Library: React  
- Build Tool: Vite  
- Styling: CSS / Standard Styles

**Database & Infrastructure:**
- Database: PostgreSQL 16 (Driver v42.7.5)  
- Extensions: PostGIS (for geospatial data processing)  
- Deployment: Docker, Docker Compose  
- Web Server: Nginx (Frontend delivery)

## ⚙️ Getting Started ##
**Prerequisites**. Make sure you have the following installed:
- Docker  
- Docker Compose
  
**Installation & Run**
- *Clone the repository:*
    1. Bash
    2. git clone <repository-url>
    3. cd <project-folder>
- *Launch the environment:*
    Execute the following command in the root directory:  
    1. Bash  
    2. docker compose up --build  
- *Wait for the build process:*
    The system will automatically download images, build the backend (Maven), compile the frontend (Vite), and initialize the database.  

## 🌐 Service Access ## 
Service________URL_External  
Frontend_______http://localhost:5173   
Backend API____http://localhost:8080  
Database_______localhost 5433   

## 🔐 Credentials ## 
Use these default credentials to access the administrative panel:  
- Role: Administrator  
- Username: admin@test.com  
- Password: admin

## 🧾 Sales Tax calculation (New York State) ##
This project fixes the missing sales tax calculation for Instant Wellness Kits orders.  
Given a delivery point **(latitude, longitude)** inside New York State and subtotal, the system determines the applicable composite sales tax rate and computes:  
- composite_tax_rate *(e.g. 0.08875)*  
- tax_amount *(rounded to 2 decimals)*  
- total_amount = *subtotal + tax_amount*  
- breakdown:  
    1. state_rate  
    2. county_rate  
    3. city_rate  
    4. special_rate (MCTD)  
- jurisdictions list *(STATE / COUNTY / CITY / SPECIAL)*

## 📊 Data sources and approach ##
We rely on publicly available NY tax data **(NY Pub 718)** and **NYC/MCTD rules**.  
To map coordinates to a tax jurisdiction, we use **PostGIS** polygons:  
- ny_state — *polygon of NY State (to validate in-state deliveries)*  
- ny_county — *county polygons*  
- ny_muni — *municipal polygons (cities/towns); additionally optimized into ny_muni_subdivided using ST_Subdivide for faster point-in-polygon checks on import*

Tax rates are seeded into:  
- ny_tax_rate with jurisdiction_type:  
  1. COUNTY — *county total tax rate*  
  2. CITY — *city total tax rate (only for specific cases)*  
  3. NYC — *New York City total tax rate (covers all 5 boroughs)*  
We store total rates *(already composite for that jurisdiction)*, then derive breakdown values by subtraction rules described below.


## 🗺 Jurisdiction resolution (by coordinates) ##  
For each order we resolve:  
- inNy — *point inside ny_state polygon*  
- countyName — *the matching ny_county polygon*  
- muniName / muniType — *the matching municipal polygon*  
- inMctd — *derived from county membership (MCTD counties set)*  
- countyTotalRate / cityTotalRate — *looked up from ny_tax_rate*

Implementation uses spatial predicates **(ST_Contains / ST_Intersects)** and GiST indexes to keep imports fast.  

## 💰 Tax rules implemented ## 
**Base rates**  
- NY State base sales tax is always:  
  1. state_rate = 0.04000 **(4%)**  
- MCTD special rate applies only if the county belongs to MCTD:  
  1. special_rate = 0.00375 **(0.375%)**  
  2. otherwise special_rate = **0**  


## 🏘 Non-NYC counties (regular case) ##  
- county_rate = *max(county_total_rate - state_rate - special_rate, 0)*  
- city_rate = *max(city_total_rate - county_total_rate, 0)*  
(only when city total rate exists; otherwise 0)  

This ensures the final composite equals:  
- state_rate + county_rate + city_rate + special_rate

## 🗽 New York City (special case) ##  
NYC is treated as a special jurisdiction covering boroughs (counties):  
**Bronx, Kings, New York, Queens, Richmond**  
*For NYC deliveries:*  
- we treat “city” as the component above state + MCTD:
  1. city_rate = max(nyc_total_rate - state_rate - special_rate, 0)
- county_rate = 0 (NYC is handled as city-level)
- city_total_rate is not required

## 🔢 Output rounding ##  
- tax_amount = ROUND(subtotal * composite_tax_rate, 2) using HALF_UP  
- total_amount = subtotal + tax_amount  
*All rates are stored with 5 decimal scale (e.g. NUMERIC(8,5)).*

## 📍 Handling coordinates outside NY State##  
**Business requirement:** orders must have delivery coordinates inside NY State.  
**We enforce this as:**  
*Manual create (POST /orders)*  
- If inNy == false → throw InvalidLocationException  
- Order is not saved.

*CSV import (POST /orders/import)*  
- Rows with inNy == false are skipped (not inserted).  
- Import succeeds and returns the number of inserted rows.

This prevents polluting the database with out-of-scope locations while keeping bulk imports resilient.

## ⏱ Timestamp handling##  
**The database stores timestamps as TIMESTAMPTZ (UTC).**  
If a timestamp is missing during manual create, the server uses Instant.now().  
*(If timestamps in CSV are local NY time, they are converted to UTC during import; final stored values are UTC.)*  

## 📌 Assumptions ##  
- Only deliveries within New York State are relevant.  
- If municipal (city/town) match is missing or no city rate exists in the rate table, we treat city_rate = 0.  
- NYC is handled as a special jurisdiction (NYC total rate applies for all 5 boroughs).  
- For points near borders/water boundaries, polygon containment rules apply as provided by the GeoJSON sources.  


