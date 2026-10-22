"""请求级上下文:contextvars 给每个请求一份独立的临时存储。

为什么不能全局变量:FastAPI 是异步并发模型,多个用户请求同时进来,
全局变量里的 token 会串号——A 的请求读到 B 的 token,就是越权事故。
contextvars 按"执行上下文"隔离,每个请求各拿各的(类比 Java 的 ThreadLocal,异步下也安全)。
"""
from contextvars import ContextVar

# 当前请求携带的用户 token:api.py 在请求开始时写入,订单工具读取后透传给网关
current_token: ContextVar[str | None] = ContextVar("current_token", default=None)


def current_headers() -> dict[str, str]:
    """构造转发给网关的请求头:带了 token 就透传,没带就空。

    身份透传原则:Agent 只搬运身份,不解读、不存储、不信任任何自报身份。
    验签是网关的职责,Agent 不做。
    """
    token = current_token.get()
    if token:
        return {"Authorization": f"Bearer {token}"}
    return {}
