FROM python:3.11-slim
WORKDIR /app

COPY requirements.txt ./
RUN apt-get update && \
    apt-get install -y build-essential gfortran libatlas3-base --no-install-recommends && \
    rm -rf /var/lib/apt/lists/* && \
    pip install --no-cache-dir -r requirements.txt

COPY . /app

ENV PORT=5060
EXPOSE 5060
CMD ["python", "app.py"]
