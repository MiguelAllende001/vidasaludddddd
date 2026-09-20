// Reemplaza con tus IDs reales de Azure AD o variables de entorno
export const msalConfig = {
  auth: {
    clientId: import.meta.env.VITE_AZURE_CLIENT_ID || "TU_AZURE_CLIENT_ID",
    authority: `https://login.microsoftonline.com/${import.meta.env.VITE_AZURE_TENANT_ID || "TU_AZURE_TENANT_ID"}`,
    redirectUri: window.location.origin,
  },
  cache: {
    cacheLocation: "sessionStorage",
    storeAuthStateInCookie: false,
  }
};

export const loginRequest = {
  scopes: ["User.Read", `api://${import.meta.env.VITE_AZURE_CLIENT_ID || "TU_AZURE_CLIENT_ID"}/access_as_user`]
};