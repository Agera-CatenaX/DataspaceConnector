# Run e2e tests locally

```bash
docker-compose -f docker-compose-consumer-provider.yml up --remove-orphans --abort-on-container-exit
```

Wait for applications to start:

```
consumer-dataspace-connector | 2021-09-01T09:52:04,909 [main] INFO - Started ConnectorApplication in 305.332 seconds (JVM running for 352.076)
provider-dataspace-connector | 2021-09-01T09:52:05,765 [main] INFO - Started ConnectorApplication in 299.515 seconds (JVM running for 349.768)
```

```bash
pipenv sync
pipenv shell
scripts/tests/contract_negotation_allow_access.py
```

