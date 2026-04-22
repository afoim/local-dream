#!/usr/bin/env python3
# -*- coding: utf-8 -*-
# /// script
# dependencies = ["pyyaml"]
# ///
"""
将YAML格式的提示词文件转换为JSON格式
用于Android App
"""

import yaml
import json
import sys
import os

def convert_yaml_to_json(yaml_file, json_file):
    """
    转换YAML到JSON
    """
    try:
        # 读取YAML文件
        with open(yaml_file, 'r', encoding='utf-8') as f:
            data = yaml.safe_load(f)
        
        # 写入JSON文件，确保UTF-8编码
        with open(json_file, 'w', encoding='utf-8', newline='\n') as f:
            json.dump(data, f, ensure_ascii=False, indent=2)
        
        print(f"✅ 转换成功: {yaml_file} -> {json_file}")
        return True
    except Exception as e:
        print(f"❌ 转换失败: {e}")
        return False

def main():
    # 输入输出文件路径
    yaml_file = "demo/sd-webui-prompt-all-in-one-main/group_tags/zh_CN.yaml"
    json_file = "app/src/main/assets/prompts/zh_CN.json"
    
    # 确保输出目录存在
    os.makedirs(os.path.dirname(json_file), exist_ok=True)
    
    # 执行转换
    if convert_yaml_to_json(yaml_file, json_file):
        # 检查文件大小
        size = os.path.getsize(json_file)
        print(f"📦 JSON文件大小: {size / 1024:.2f} KB")
    else:
        sys.exit(1)

if __name__ == "__main__":
    main()
