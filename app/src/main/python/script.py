import pandas as pd
import joblib
def main(file_path):
  model = joblib.load("model.joblib")
  data = pd.read_csv(file_path + ".csv")
  if "Stage" not in data.columns:
    data["Stage"] = model.predict(data.loc[:, data.columns != 'Time'])
    data["Stage"].replace({0: "W", 1: "LS", 2: "DS", 3: "REM"}, inplace=True)
    data_summary = data.groupby("Stage").size()
    data_summary.to_json(file_path + ".json")
    data.to_csv(file_path + ".csv")

