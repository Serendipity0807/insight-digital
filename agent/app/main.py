import logging

from fastapi import FastAPI

from app.api import router

#打开信息级日志
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(name)s: %(message)s",
)

app = FastAPI(title = "insight-digital")

app.include_router(router)

@app.get("/health")
def health():
    return {"status": "ok"}