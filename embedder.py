import json
import gensim.downloader as api
from sklearn.decomposition import PCA
import numpy as np

# ברירת מחדל: 5000 מילים
limit = 5000

# מודל GloVe עם 100 ממדים
gensim_model_name = "glove-wiki-gigaword-100"

print("--- Loading GloVe model (this may take a minute on first run)... ---")
model = api.load(gensim_model_name)

# ניקח את 5000 המילים הנפוצות ביותר
words = model.index_to_key[:limit]
full_vectors = [model[word].tolist() for word in words]

print("--- Performing PCA (100D -> 50D)... ---")
pca = PCA(n_components=50)
pca_result = pca.fit_transform(np.array(full_vectors))

# הכנת מבני הנתונים ל-JSON
full_space_data = []
pca_space_data = []

for i, word in enumerate(words):
    full_space_data.append({
        "word": word,
        "vector": full_vectors[i]
    })
    pca_space_data.append({
        "word": word,
        "vector": pca_result[i].tolist()
    })

print("--- Saving files... ---")

with open("full_vectors.json", "w", encoding="utf-8") as f:
    json.dump(full_space_data, f, ensure_ascii=False)

with open("pca_vectors.json", "w", encoding="utf-8") as f:
    json.dump(pca_space_data, f, ensure_ascii=False)

print("--- Success! Created 'full_vectors.json' and 'pca_vectors.json' ---")
print(f"--- Number of words: {len(words)} ---")