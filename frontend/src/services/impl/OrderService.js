    import BaseService from "../BaseService";

    class OrderService extends BaseService {
        constructor() {
            super("/orders");
        }

        create(orderData) {
            return this.post("", orderData);
        }

        importCsv(file) {
            const formData = new FormData();
            formData.append("file", file);

            return this.post("/import", formData, {
                headers: {
                    "Content-Type": "multipart/form-data"
                }
            });
        }

        getAll(
            filter = {},
            page = 0,
            size = 10,
            sort = "timestamp,desc"
        ) {
            const params = this.prepareParams(filter, page, size, sort);

            return this.get("", { params });
        }
    }

    export default new OrderService();