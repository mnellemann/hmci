# Prometheus Notes

Enable prometheus in the *hmci.toml* configuration file:

```toml
[prometheus]
port = 9040
```


Add *hmci* as a scraping target in your *prometheus.yml*, such as:

```yaml
scrape_configs:
  - job_name: "hmci"
    static_configs:
      - targets: ["localhost:9040"]
```