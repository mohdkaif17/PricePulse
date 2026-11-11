import sys
import os
import pytest

# Add backend directory to sys.path
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))

from app.db.session import engine

@pytest.fixture(autouse=True)
def dispose_engine_pool():
    yield
    engine.sync_engine.dispose()

