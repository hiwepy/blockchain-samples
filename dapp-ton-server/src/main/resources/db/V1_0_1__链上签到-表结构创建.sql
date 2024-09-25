
-- ----------------------------
-- Table structure for t_config_chain_daily_sign
-- ----------------------------
CREATE TABLE `t_config_chain_daily_sign` (
   `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
   `version` int NOT NULL DEFAULT '0' COMMENT '版本',
   `day` int(11) DEFAULT 0 COMMENT '签到天数',
   `reward` decimal(16,4) NOT NULL DEFAULT '0.0000' COMMENT '奖励',
   `created_at` bigint NOT NULL DEFAULT '0' COMMENT '创建时间',
   `updated_at` bigint NOT NULL DEFAULT '0' COMMENT '更新时间',
   PRIMARY KEY (`id`) USING BTREE,
   KEY `idx_day` (`day`) USING BTREE COMMENT '链上签到奖励配置索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='链上每日签到奖励配置表';

-- ----------------------------
-- Table structure for t_chain_daily_sign_user
-- ----------------------------
CREATE TABLE `t_chain_daily_sign_user` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `version` int NOT NULL DEFAULT '0' COMMENT '版本',
    `user_id` bigint(20) NOT NULL COMMENT '用户ID（关联用户表的ID）',
    `series_days` int(11) DEFAULT '0' COMMENT '累计签到天数',
    `duration_days` int(11) DEFAULT '0' COMMENT '连续签到天数',
    `last_sign_in_at` bigint NOT NULL DEFAULT '0' COMMENT '最后一次签到时间',
    `created_at` bigint NOT NULL DEFAULT '0' COMMENT '创建时间',
    `updated_at` bigint NOT NULL DEFAULT '0' COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_user` (`user_id`) USING BTREE COMMENT '链上签到用户索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='链上每日用户签到表';

-- ----------------------------
-- Table structure for t_chain_daily_sign_log
-- ----------------------------
CREATE TABLE `t_chain_daily_sign_log` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` bigint(20) NOT NULL COMMENT '用户ID（关联用户表的ID）',
    `user_client_id` varchar(128) NOT NULL COMMENT '客户端id',
    `address` varchar(50) NOT NULL COMMENT '钱包地址',
    `hash_address` varchar(50) NOT NULL COMMENT '交易hash地址',
    `sign_in_at` datetime NOT NULL COMMENT '签到时间',
    `reward_amount` decimal(16,4) NOT NULL DEFAULT '0.0000' COMMENT '签到奖励',
    `reward_content` text NULL COMMENT '签到奖励内容',
    `created_at` bigint NOT NULL DEFAULT '0' COMMENT '创建时间',
    `updated_at` bigint NOT NULL DEFAULT '0' COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_user` (`user_id`) USING BTREE COMMENT '链上签到记录索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='链上每日用户签到记录表';
