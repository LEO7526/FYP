function drawFloorPlan(ctx, tables) {
    ctx.clearRect(0, 0, ctx.canvas.width, ctx.canvas.height);

    tables.forEach(table => {
        ctx.fillStyle = table.selected ? "#ffeb3b" : (table.available ? "#a5d6a7" : "#ef9a9a");
        ctx.fillRect(table.x, table.y, 60, 40);

        ctx.fillStyle = "#000";
        ctx.font = "bold 14px sans-serif";
        ctx.textAlign = "center";
        ctx.textBaseline = "middle";
        ctx.fillText(table.tid, table.x + 30, table.y + 20);
    });
}

function initFloorPlan(canvasId, hiddenInputId, tables) {
    const canvas = document.getElementById(canvasId);
    const ctx = canvas.getContext("2d");

    drawFloorPlan(ctx, tables);

    canvas.addEventListener("click", function (e) {
        const rect = canvas.getBoundingClientRect();
        const x = e.clientX - rect.left;
        const y = e.clientY - rect.top;

        tables.forEach(table => {
            if (x >= table.x && x <= table.x + 60 &&
                y >= table.y && y <= table.y + 40) {
                if (!table.available) {
                    alert(`座位 ${table.tid} 不可用`);
                    return;
                }
                tables.forEach(t => t.selected = false);
                table.selected = true;
                document.getElementById(hiddenInputId).value = table.tid.replace("T", "");
                drawFloorPlan(ctx, tables);
            }
        });
    });

    return { ctx, tables };
}
