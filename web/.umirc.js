import { defineConfig } from 'umi';

export default defineConfig({
    define: {
        "process.env.IS_DEBUG": process.env.IS_DEBUG
    },
    nodeModulesTransform: {
        type: 'none',
    },
    routes: [
        { path: '/', component: '@/pages/index' },
    ],
    fastRefresh: {},
});
