import os
from typing import List, Optional, Union
import urllib.parse
from urllib.parse import quote_plus
from pydantic import AnyHttpUrl, field_validator
from pydantic_settings import BaseSettings, SettingsConfigDict

class Settings(BaseSettings):
    PROJECT_NAME: str = "PricePulse API"
    VERSION: str = "1.0.0"
    API_V1_STR: str = "/api/v1"
    APP_ENV: str = "development"
    APP_DEBUG: bool = True
    API_PORT: int = 8000
    HOST_IP_FOR_EMULATOR: str = "10.0.2.2"

    # Database Configuration (supports both unified DATABASE_URL and discrete DB_* variables)
    DATABASE_URL: Optional[str] = None
    DB_HOST: str = "localhost"
    DB_PORT: int = 5432
    DB_NAME: str = "price_comparison"
    DB_USER: str = "postgres"
    DB_PASSWORD: str = ""
    DB_SCHEMA: str = "public"
    DB_SSL: Optional[bool] = None

    # Connection Pooling (configurable for Render and production environments)
    DB_POOL_SIZE: int = 5
    DB_MAX_OVERFLOW: int = 10

    # Security
    SECRET_KEY: str = "pricepulse_super_secret_jwt_key_development_only_2026_change_in_production"
    ALGORITHM: str = "HS256"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 60 * 24  # 1 day

    # CORS
    BACKEND_CORS_ORIGINS: List[str] = ["*"]

    model_config = SettingsConfigDict(
        env_file=(".env", "../.env", "backend/.env"),
        env_file_encoding="utf-8",
        extra="ignore"
    )

    @property
    def require_ssl(self) -> bool:
        """Determines whether SSL is required based on explicit setting, URL params, or remote host."""
        if self.DB_SSL is not None:
            return self.DB_SSL
        if self.DATABASE_URL:
            parsed = urllib.parse.urlsplit(self.DATABASE_URL)
            query_lower = parsed.query.lower()
            if "sslmode=require" in query_lower or "ssl=require" in query_lower or "ssl=true" in query_lower:
                return True
            if parsed.hostname and ("render.com" in parsed.hostname.lower() or parsed.hostname not in (
                "localhost", "127.0.0.1", "::1", "0.0.0.0"
            )):
                return True
        elif self.DB_HOST not in ("localhost", "127.0.0.1", "::1", "0.0.0.0"):
            return True
        return False

    @property
    def sync_database_url(self) -> str:
        """Normalized synchronous URL for psycopg driver."""
        if self.DATABASE_URL:
            parsed = urllib.parse.urlsplit(self.DATABASE_URL)
            return urllib.parse.urlunsplit(('postgresql+psycopg', parsed.netloc, parsed.path, parsed.query, parsed.fragment))
        encoded_pass = quote_plus(self.DB_PASSWORD)
        return f"postgresql+psycopg://{self.DB_USER}:{encoded_pass}@{self.DB_HOST}:{self.DB_PORT}/{self.DB_NAME}"

    @property
    def async_database_url(self) -> str:
        """Normalized asynchronous URL for asyncpg driver with sslmode query stripped."""
        if self.DATABASE_URL:
            parsed = urllib.parse.urlsplit(self.DATABASE_URL)
            q_pairs = urllib.parse.parse_qsl(parsed.query, keep_blank_values=True)
            # asyncpg does not accept libpq 'sslmode' query param; strip it so it doesn't fail
            async_q = [(k, v) for k, v in q_pairs if k.lower() not in ('sslmode',)]
            async_query_str = urllib.parse.urlencode(async_q)
            return urllib.parse.urlunsplit(('postgresql+asyncpg', parsed.netloc, parsed.path, async_query_str, parsed.fragment))
        encoded_pass = quote_plus(self.DB_PASSWORD)
        return f"postgresql+asyncpg://{self.DB_USER}:{encoded_pass}@{self.DB_HOST}:{self.DB_PORT}/{self.DB_NAME}"

settings = Settings()
