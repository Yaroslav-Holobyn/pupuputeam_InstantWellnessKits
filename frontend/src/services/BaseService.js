import {client} from "../utils/client.js";

class BaseService {
    constructor(endpoint) {
        this.endpoint = endpoint;
    }

    async handleRequest(request) {
        try {
            const response = await request();
            return response.data;
        } catch (error) {
            throw error;
        }
    }

    prepareParams(filter = {}, page = 0, size = 10, sort = null) {
        const cleanFilter = Object.fromEntries(
            Object.entries(filter).filter(([_, v]) => v != null && v !== "")
        );

        const params = {
            page,
            size,
            ...cleanFilter
        };

        if (sort) {
            params.sort = sort;
        }

        return params;
    }

    get(path, config = {}) {
        return this.handleRequest(() => client.get(`${this.endpoint}${path}`, config));
    }

    post(path, data = {}, config = {}) {
        return this.handleRequest(() => client.post(`${this.endpoint}${path}`, data, config));
    }

    patch(path, data = {}, config = {}) {
        return this.handleRequest(() => client.patch(`${this.endpoint}${path}`, data, config));
    }

    put(path, data = {}, config = {}) {
        return this.handleRequest(() => client.put(`${this.endpoint}${path}`, data, config));
    }

    delete(path, config = {}) {
        return this.handleRequest(() => client.delete(`${this.endpoint}${path}`, config));
    }
}

export default BaseService;