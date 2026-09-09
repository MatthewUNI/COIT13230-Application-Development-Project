import json
import requests
import os

# Load API key from environment variable to ensure security
API_KEY = os.getenv("GENAI_API_KEY")
URL = f"https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key={API_KEY}"

def test_meal_plan_prompt():
    headers = {'Content-Type': 'application/json'}
    
    # Strict prompt engineering test
    prompt = """
    Create a 1-day vegan meal plan. 
    You must only use these ingredients: Rice, Beans, Broccoli, Tofu.
    Output strictly as a JSON object with keys: 'meals', 'ingredients'.
    """
    
    payload = {
        "contents": [{"parts": [{"text": prompt}]}]
    }

    try:
        response = requests.post(URL, headers=headers, data=json.dumps(payload))
        response.raise_for_status()
        
        # Test if the LLM hallucinated conversational text outside the JSON block
        json_response = response.json()
        print("API Connection Successful!")
        print(json.dumps(json_response, indent=2))
        
    except requests.exceptions.RequestException as e:
        print(f"Network or API Error: {e}")
    except json.JSONDecodeError:
        print("Graceful Failure: The model hallucinated invalid JSON formatting.")

if __name__ == "__main__":
    if API_KEY:
        test_meal_plan_prompt()
    else:
        print("Error: API Key not found in environment.")
