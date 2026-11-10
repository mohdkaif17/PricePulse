import os
from typing import List, Union
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

    # Database
    DB_HOST: str = "localhost"
    DB_PORT: int = 5432
    DB_NAME: str = "price_comparison"
    DB_USER: str = "postgres"
    DB_PASSWORD: str = ""
    DB_SCHEMA: str = "public"

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
    def sync_database_url(self) -> str:
        encoded_pass = quote_plus(self.DB_PASSWORD)
        return f"postgresql+psycopg://{self.DB_USER}:{encoded_pass}@{self.DB_HOST}:{self.DB_PORT}/{self.DB_NAME}"

    @property
    def async_database_url(self) -> str:
        encoded_pass = quote_plus(self.DB_PASSWORD)
        return f"postgresql+asyncpg://{self.DB_USER}:{encoded_pass}@{self.DB_HOST}:{self.DB_PORT}/{self.DB_NAME}"

settings = Settings()
