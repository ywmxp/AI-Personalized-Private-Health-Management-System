"""直接调FastAPI，检查中文是否有问题。排除PowerShell干扰"""
import json
import urllib.request
import sys

url = sys.argv[1] if len(sys.argv) > 1 else "http://127.0.0.1:30000/api/ai/profile"

body = json.dumps({
    "userId": 2,
    "healthData": [
        {"dataType": "SLEEP_HOURS", "dataValue": "5", "recordTime": "2026-05-27 08:00:00"},
        {"dataType": "WEIGHT", "dataValue": "78", "recordTime": "2026-05-27 08:00:00"},
    ],
    "timeRange": "LAST_7_DAYS",
}).encode()

req = urllib.request.Request(url, data=body, headers={"Content-Type": "application/json"})
resp = urllib.request.urlopen(req)
data = json.loads(resp.read().decode("utf-8"))

# 用 ascii + replace 策略打印，即使终端不支持也不会报错
print("=== FastAPI 原始响应 ===")
for k, v in data.items():
    if isinstance(v, list):
        print(f"  {k}: {v}")
    elif isinstance(v, str):
        print(f"  {k}: {v}")

# 判断中文是否正常
all_text = json.dumps(data, ensure_ascii=False)
has_chinese = any('一' <= c <= '鿿' for c in all_text)
has_escape = '\\u' in json.dumps(data, ensure_ascii=True) and '\\u' not in all_text

print()
if has_chinese:
    print("✓ 响应中包含中文字符，编码正常")
    print(f"  analysis 前80字: {data.get('analysis', 'N/A')[:80]}")
else:
    print("✗ 响应中没有发现中文字符，可能有问题")
    print(f"  原始内容: {all_text[:200]}")
