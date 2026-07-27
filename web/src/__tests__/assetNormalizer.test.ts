import { describe, it, expect } from 'vitest';
import { normalizeAssetsResponse } from '../utils/assetNormalizer';

describe('normalizeAssetsResponse', () => {
  it('handles spring-style content response', () => {
    const resp = {
      content: [
        {
          id: 1,
          assetTag: 'APX-FL-001',
          name: 'Warehouse Forklift #1',
          qrCodeId: 'SN-APX-FL-001',
          status: 'ACTIVE',
        },
      ],
      totalElements: 1,
    };

    const { items, total } = normalizeAssetsResponse(resp);
    expect(total).toBe(1);
    expect(items.length).toBe(1);
    expect(items[0].name).toBe('Warehouse Forklift #1');
    expect(items[0].qr_code_id).toBe('SN-APX-FL-001');
  });
});
