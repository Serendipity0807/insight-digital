from pathlib import Path

from pydantic_settings import BaseSettings, SettingsConfigDict

BASE_DIR = Path(__file__).resolve().parent.parent

class Settings(BaseSettings):
    """
    全局配置:pydantic-settings 自动从环境变量和 .env 读取。

    字段名 deepseek_api_key 会自动匹配环境变量 DEEPSEEK_API_KEY(大小写不敏感)。
    """

    model_config = SettingsConfigDict(
        env_file=BASE_DIR / ".env",
        env_file_encoding="utf-8",
    )

    deepseek_api_key: str
    deepseek_base_url: str = "https://api.deepseek.com"
    deepseek_model: str = "deepseek-flash"

settings = Settings()