<?php
$token = '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';

// 正确的API端点 - 获取Image目录内容
$ch = curl_init();
curl_setopt($ch, CURLOPT_URL, 'https://api.github.com/repos/LEO7526/FYP/contents/Image');
curl_setopt($ch, CURLOPT_HTTPHEADER, [
    'Authorization: token ' . $token,
    'User-Agent: PHP-Script'
]);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
$response = curl_exec($ch);
$httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);

echo "HTTP状态码: $httpCode\n";
echo "响应:\n$response\n";

if ($httpCode === 200) {
    echo "\n✅ 令牌有效，有权限访问仓库\n";
} elseif ($httpCode === 401) {
    echo "\n❌ 令牌无效或已过期\n";
} elseif ($httpCode === 403) {
    echo "\n❌ 令牌权限不足或达到速率限制\n";
} elseif ($httpCode === 404) {
    echo "\n❌ 仓库不存在或路径错误\n";
}