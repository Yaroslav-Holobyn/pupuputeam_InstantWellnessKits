export const extractErrorMessage = (error) => {
    if (!error) return "Unknown error";

    if (!error.response) {
        return "Server unavailable. Please check your internet connection.";
    }

    const data = error.response.data;

    if (data && Array.isArray(data.messages) && data.messages.length > 0) {
        return data.messages.join("\n");
    }

    if (data && data.message) {
        return data.message;
    }

    switch (error.response.status) {
        case 400: return "Bad Request (400)";
        case 401: return "Unauthorized (401)";
        case 403: return "Access denied (403)";
        case 404: return "Resource not found (404)";
        case 500: return "Internal Server Error (500)";
        default:  return "An unexpected error occurred";
    }
};