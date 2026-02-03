
#!/bin/bash
set -e

# 1. Rename files in inventory-service (Product -> Inventory)
find microservices/inventory-service -name "Product*" | while read f; do
    new_name=$(echo "$f" | sed 's/Product/Inventory/g')
    mv "$f" "$new_name"
done

# 2. Setup API module for Inventory
mkdir -p api/src/main/java/se/magnus/api/core/inventory
cp -r api/src/main/java/se/magnus/api/core/product/* api/src/main/java/se/magnus/api/core/inventory/

# 3. Rename files in API (Product -> Inventory)
find api/src/main/java/se/magnus/api/core/inventory -name "Product*" | while read f; do
    new_name=$(echo "$f" | sed 's/Product/Inventory/g')
    mv "$f" "$new_name"
done

# 4. Replace content in API files (Product -> Inventory)
# Careful with case sensitivity.
find api/src/main/java/se/magnus/api/core/inventory -type f -exec sed -i 's/Product/Inventory/g' {} +
find api/src/main/java/se/magnus/api/core/inventory -type f -exec sed -i 's/product/inventory/g' {} +

# 5. Fix package declaration in new API files
# They were copied from core/product, so they have "package se.magnus.api.core.product;"
# Need "package se.magnus.api.core.inventory;"
find api/src/main/java/se/magnus/api/core/inventory -type f -exec sed -i 's/package se.magnus.api.core.product;/package se.magnus.api.core.inventory;/g' {} +

