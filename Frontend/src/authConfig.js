// Reemplaza con tus IDs reales de Azure AD o variables de entorno
export const msalConfig = {
    auth: {
        clientId: import.meta.env.VITE_AZURE_CLIENT_ID || "2460c449-b914-47fb-8a1c-58da535aba2d",
        authority: `https://login.microsoftonline.com/${import.meta.env.VITE_AZURE_TENANT_ID || "cd89ce2b-8f61-42ab-9a9a-bf532acace5d"}`,
        redirectUri: window.location.origin,
    },
    cache: {
        cacheLocation: "sessionStorage",
        storeAuthStateInCookie: false,
    }
};

export const loginRequest = {
    scopes: ["User.Read", `api://${import.meta.env.VITE_AZURE_CLIENT_ID || "2460c449-b914-47fb-8a1c-58da535aba2d"}/access_as_user`]
}; 