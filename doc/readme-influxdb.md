# InfluxDB Notes


## Delete data

To delete *all* data before a specific date, run:

```sql
DELETE WHERE time < '2023-01-01'
```
