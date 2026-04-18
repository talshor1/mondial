import { useState } from "react";
import { useNavigate, Navigate } from "react-router-dom";
import { GoogleLogin } from "@react-oauth/google";
import { useAuth } from "../AuthContext";

export default function LoginPage() {
  const { loginWithGoogle, token } = useAuth();
  const navigate = useNavigate();
  const [error, setError] = useState("");
  if (token) return <Navigate to="/" replace />;

  const handleGoogleSuccess = async (credentialResponse: any) => {
    try {
      await loginWithGoogle(credentialResponse.credential);
      navigate("/");
    } catch (err: any) {
      setError("Google login failed. Please try again.");
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-card">
        <h1 className="auth-title">Welcome back</h1>
        <p className="auth-subtitle">Sign in with your Google account</p>

        {error && <p className="error-msg">{error}</p>}

        <div style={{ display: "flex", justifyContent: "center", marginTop: "1.5rem" }}>
          <GoogleLogin
            onSuccess={handleGoogleSuccess}
            onError={() => setError("Google login failed. Please try again.")}
            text="signin_with"
            shape="rectangular"
            theme="outline"
          />
        </div>
      </div>
    </div>
  );
}