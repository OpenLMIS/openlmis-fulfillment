import xml.etree.ElementTree as ET

# Parse the XML file
tree = ET.parse('/home/user/Desktop/LMIS/openlmis-fulfillment/build/performance-artifacts/stats.xml')
root = tree.getroot()

# Function to truncate text to a maximum length
def truncate(text, max_length):
    return text if len(text) <= max_length else text[:max_length-3] + '...'

# Set maximum lengths for certain fields
MAX_LABEL_LENGTH = 30
MAX_HTTP_CODES_LENGTH = 20

# Print table headers with fixed widths
print('{:<{label_width}} {:>{samples_width}} {:>{avg_rt_width}} {:>{min_width}} {:>{median_width}} {:>{perc90_width}} {:>{max_width}} {:<{http_codes_width}} {:>{errors_width}} {:>{avg_kb_width}} {:>{total_kb_width}}'.format(
    'Label', 'Samples', 'Avg (ms)', 'Min', 'Median', '90th %', 'Max', 'HTTP Codes', 'Errors (%)', 'Avg (KB)', 'Total (KB)',
    label_width=MAX_LABEL_LENGTH,
    samples_width=8,
    avg_rt_width=10,
    min_width=8,
    median_width=10,
    perc90_width=10,
    max_width=8,
    http_codes_width=MAX_HTTP_CODES_LENGTH,
    errors_width=10,
    avg_kb_width=10,
    total_kb_width=12))

# Iterate over each Group
for group in root.findall('Group'):
    label = group.get('label')
    label = truncate(label, MAX_LABEL_LENGTH)  # Truncate label if necessary
    
    # Successes and Failures
    succ = int(group.find('succ').get('value'))
    fail = int(group.find('fail').get('value'))
    samples = succ + fail

    # Response Times (convert to milliseconds)
    avg_rt = float(group.find('avg_rt').get('value')) * 1000  # Average response time
    perc_elements = group.findall('perc')
    perc_values = {}
    for perc in perc_elements:
        param = perc.get('param')
        value = float(perc.get('value')) * 1000  # Convert to milliseconds
        perc_values[param] = value

    min_rt = perc_values.get('0.0', 0.0)
    median_rt = perc_values.get('50.0', 0.0)
    perc90_rt = perc_values.get('90.0', 0.0)
    max_rt = perc_values.get('100.0', 0.0)

    # HTTP Codes
    rc_elements = group.findall('rc')
    http_codes = {}
    for rc in rc_elements:
        code = rc.get('param')
        count = int(rc.get('value'))
        http_codes[code] = count

    # Format HTTP Codes for display
    http_codes_str = ', '.join([f'{code} ({count})' for code, count in http_codes.items()])
    http_codes_str = truncate(http_codes_str, MAX_HTTP_CODES_LENGTH)

    # Errors (%)
    errors_pct = (fail / samples * 100) if samples > 0 else 0.0

    # Bytes (convert to KB)
    total_bytes_element = group.find('bytes')
    total_bytes = int(total_bytes_element.get('value')) if total_bytes_element is not None else 0
    total_kb = total_bytes / 1024
    avg_kb = (total_bytes / samples / 1024) if samples > 0 else 0.0

    # Print the data with fixed widths
    print('{:<{label_width}} {:>{samples_width}} {:>{avg_rt_width}.2f} {:>{min_width}.2f} {:>{median_width}.2f} {:>{perc90_width}.2f} {:>{max_width}.2f} {:<{http_codes_width}} {:>{errors_width}.2f} {:>{avg_kb_width}.2f} {:>{total_kb_width}.2f}'.format(
        label, samples, avg_rt, min_rt, median_rt, perc90_rt, max_rt, http_codes_str, errors_pct, avg_kb, total_kb,
        label_width=MAX_LABEL_LENGTH,
        samples_width=8,
        avg_rt_width=10,
        min_width=8,
        median_width=10,
        perc90_width=10,
        max_width=8,
        http_codes_width=MAX_HTTP_CODES_LENGTH,
        errors_width=10,
        avg_kb_width=10,
        total_kb_width=12))

