import React, { useState } from 'react';
import { useMsal, useIsAuthenticated } from "@azure/msal-react";
import { loginRequest } from "./authConfig";
import axios from 'axios';

export function App() {
  const { instance, accounts } = useMsal();
  const isAuthenticated = useIsAuthenticated();
  const [apiResponse, setApiResponse] = useState(null);
  const [error, setError] = useState(null);

  // URL del API Gateway (o el BFF directamente en desarrollo)
  const API_URL = import.meta.env.VITE_API_GATEWAY_URL || "http://localhost:8080";

  const handleLogin = () => {
    instance.loginPopup(loginRequest).catch(e => console.error(e));
  };

  const handleLogout = () => {
    instance.logoutPopup().catch(e => console.error(e));
  };

  const callBackendApi = async () => {
    try {
      setError(null);
      // Obtener el JWT Token silenciosamente
      const response = await instance.acquireTokenSilent({
        ...loginRequest,
        account: accounts[0]
      });

      // Peticion HTTP al API Gateway enviando el JWT Bearer Token
      const res = await axios.get(`${API_URL}/api/appointments`, {
        headers: {
          Authorization: `Bearer ${response.accessToken}`
        }
      });

      setApiResponse(res.data);
    } catch (err) {
      console.error("Error al consultar API:", err);
      setError(err.response ? JSON.stringify(err.response.data) : err.message);
    }
  };

  return (
    <div style={{ padding: "20px", fontFamily: "Arial, sans-serif" }}>
      <h1>🏥 Plataforma VidaSalud</h1>

      {isAuthenticated ? (
        <div>
          <p><strong>Bienvenido:</strong> {accounts[0]?.name} ({accounts[0]?.username})</p>
          <button onClick={handleLogout} style={{ padding: "8px 16px", backgroundColor: "#dc3545", color: "white", border: "none", borderRadius: "4px", cursor: "pointer" }}>
            Cerrar Sesión
          </button>

          <hr style={{ margin: "20px 0" }} />

          <h2>Prueba de Integración API Gateway / BFF</h2>
          <button onClick={callBackendApi} style={{ padding: "10px 20px", backgroundColor: "#0d6efd", color: "white", border: "none", borderRadius: "4px", cursor: "pointer" }}>
            Consultar Atenciones (/api/appointments)
          </button>

          {apiResponse && (
            <div style={{ marginTop: "20px", background: "#f8f9fa", padding: "15px", borderRadius: "5px" }}>
              <h3>Respuesta del Backend:</h3>
              <pre>{JSON.stringify(apiResponse, null, 2)}</pre>
            </div>
          )}

          {error && (
            <div style={{ marginTop: "20px", background: "#f8d7da", color: "#721c24", padding: "15px", borderRadius: "5px" }}>
              <h3>Error:</h3>
              <p>{error}</p>
            </div>
          )}
        </div>
      ) : (
        <div>
          <p>Debes iniciar sesión con tu cuenta corporativa de Microsoft (Azure AD) para continuar.</p>
          <button onClick={handleLogin} style={{ padding: "10px 20px", backgroundColor: "#198754", color: "white", border: "none", borderRadius: "4px", cursor: "pointer" }}>
            Iniciar Sesión con Microsoft
          </button>
        </div>
      )}
    </div>
  );
}

export default App;